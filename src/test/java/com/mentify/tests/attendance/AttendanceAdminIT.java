package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class AttendanceAdminIT extends AttendanceIntegrationTestSupport {

    @Test
    void adminCanReadStudentSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.fixturesConfigured(), "Set approved attendance course and student IDs");
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            assertEquals(200, attendanceClient.studentSummary(data.studentId().toString(), data.courseId().toString(),
                    admin.accessToken()).statusCode());
        } finally {
            logout(admin);
        }
    }

    @Test
    void adminCanCreateAndRetrieveAttendanceSessionWhenWritesAreEnabled() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_ATTENDANCE_WRITE_ENABLED=true and approved attendance IDs to run write tests");
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response created = attendanceClient.createSession(AttendanceTestDataFactory.session(data.courseId()),
                    admin.accessToken());
            assertEquals(201, created.statusCode());
            String sessionId = created.jsonPath().getString("data.id");
            assertEquals(200, attendanceClient.getSession(sessionId, admin.accessToken()).statusCode());
            assertEquals(200, attendanceClient.cancel(sessionId, admin.accessToken()).statusCode());
        } finally {
            logout(admin);
        }
    }
}
