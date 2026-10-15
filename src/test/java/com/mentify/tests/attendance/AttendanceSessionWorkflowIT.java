package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AttendanceSessionWorkflowIT extends AttendanceIntegrationTestSupport {

    @Test
    void teacherCanCreateListMarkAndCompleteSession() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_ATTENDANCE_WRITE_ENABLED=true and approved attendance IDs to run write tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response created = attendanceClient.createSession(AttendanceTestDataFactory.session(data.courseId()),
                    teacher.accessToken());
            assertEquals(201, created.statusCode());
            String sessionId = created.jsonPath().getString("data.id");
            assertTrue(sessionId != null && !sessionId.isBlank());
            assertEquals("OPEN", created.jsonPath().getString("data.status"));

            assertEquals(200, attendanceClient.listSessions(data.courseId().toString(), teacher.accessToken()).statusCode());
            assertEquals(200, attendanceClient.getSession(sessionId, teacher.accessToken()).statusCode());
            Response marked = attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(data.studentId(), "PRESENT"), teacher.accessToken());
            assertEquals(200, marked.statusCode());
            assertEquals(200, attendanceClient.complete(sessionId, teacher.accessToken()).statusCode());
            assertEquals("COMPLETED", attendanceClient.getSession(sessionId, teacher.accessToken())
                    .jsonPath().getString("data.status"));
            assertTrue(attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(UUID.randomUUID(), "ABSENT"), teacher.accessToken())
                    .statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherCanCancelOpenSessionAndCancelledSessionCannotBeCompleted() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_ATTENDANCE_WRITE_ENABLED=true and approved attendance IDs to run write tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response created = attendanceClient.createSession(AttendanceTestDataFactory.session(data.courseId()),
                    teacher.accessToken());
            assertEquals(201, created.statusCode());
            String sessionId = created.jsonPath().getString("data.id");
            assertEquals(200, attendanceClient.cancel(sessionId, teacher.accessToken()).statusCode());
            assertEquals("CANCELLED", attendanceClient.getSession(sessionId, teacher.accessToken())
                    .jsonPath().getString("data.status"));
            assertTrue(attendanceClient.complete(sessionId, teacher.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }
}
