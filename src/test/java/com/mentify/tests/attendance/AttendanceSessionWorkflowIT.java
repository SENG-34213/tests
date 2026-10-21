package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
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
            List<String> enrolledStudentIds = created.jsonPath().getList("data.records*.studentId", String.class);
            assertTrue(enrolledStudentIds.contains(data.studentId().toString()),
                    "The configured attendance student must be enrolled in the fixture course");

            Response listed = attendanceClient.listSessions(data.courseId().toString(), teacher.accessToken());
            assertEquals(200, listed.statusCode());
            assertTrue(listed.jsonPath().getList("data*.id", String.class).contains(sessionId));
            assertEquals(200, attendanceClient.getSession(sessionId, teacher.accessToken()).statusCode());
            assertEquals(409, attendanceClient.complete(sessionId, teacher.accessToken()).statusCode(),
                    "A session with NOT_MARKED records must not be completed");
            assertEquals(404, attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(UUID.randomUUID(), "ABSENT"), teacher.accessToken()).statusCode(),
                    "A student outside the session must not be added by marking attendance");
            assertEquals(400, attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(data.studentId(), "NOT_MARKED"), teacher.accessToken()).statusCode(),
                    "NOT_MARKED is a session default, not a valid marking status");
            assertEquals(400, attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.duplicateRecords(data.studentId()), teacher.accessToken()).statusCode(),
                    "A student may appear only once in a marking batch");
            Response marked = attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(enrolledStudentIds, "PRESENT"), teacher.accessToken());
            assertEquals(200, marked.statusCode());
            assertTrue(marked.jsonPath().getList("data.records*.status", String.class)
                    .stream().allMatch("PRESENT"::equals));
            Response completed = attendanceClient.complete(sessionId, teacher.accessToken());
            assertEquals(200, completed.statusCode());
            assertEquals("COMPLETED", completed.jsonPath().getString("data.status"));
            assertEquals("COMPLETED", attendanceClient.getSession(sessionId, teacher.accessToken())
                    .jsonPath().getString("data.status"));
            assertEquals(409, attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(data.studentId(), "ABSENT"), teacher.accessToken()).statusCode(),
                    "Completed sessions must reject further marking");
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
            assertEquals(409, attendanceClient.complete(sessionId, teacher.accessToken()).statusCode());
            assertEquals(409, attendanceClient.markAttendance(sessionId,
                    AttendanceTestDataFactory.records(data.studentId(), "PRESENT"), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
