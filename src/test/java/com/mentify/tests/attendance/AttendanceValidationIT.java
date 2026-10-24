package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttendanceValidationIT extends AttendanceIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void rejectsInvalidSessionAndRecordRequests() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(400, attendanceClient.createSession(Map.of(), teacher.accessToken()).statusCode());
            assertEquals(400, attendanceClient.createSession(Map.of("courseId", "not-a-uuid", "title", " "),
                    teacher.accessToken()).statusCode());
            assertEquals(400, attendanceClient.markAttendance(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode());
            assertEquals(400, attendanceClient.markAttendance(UNKNOWN_ID,
                    Map.of("records", java.util.List.of(Map.of("studentId", "not-a-uuid", "status", "INVALID"))),
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownAttendanceResourcesReturnNotFound() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(404, attendanceClient.getSession(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, attendanceClient.listSessions(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, attendanceClient.studentSummary(UNKNOWN_ID, UNKNOWN_ID, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
