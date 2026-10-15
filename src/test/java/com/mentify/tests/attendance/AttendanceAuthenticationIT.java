package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttendanceAuthenticationIT extends AttendanceIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void protectedAttendanceRequestsRejectMissingAuthentication() {
        Map<String, Object> session = Map.of("title", "unauthenticated");
        assertEquals(401, attendanceClient.createSession(session, null).statusCode());
        assertEquals(401, attendanceClient.listSessions(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attendanceClient.getSession(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attendanceClient.markAttendance(UNKNOWN_ID, Map.of(), null).statusCode());
        assertEquals(401, attendanceClient.complete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attendanceClient.cancel(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attendanceClient.mySummary(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attendanceClient.studentSummary(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
    }

    @Test
    void studentCannotManageSessionsOrReadAnotherStudentSummary() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, attendanceClient.createSession(Map.of("title", "blocked"), student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.listSessions(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.getSession(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.complete(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.studentSummary(UNKNOWN_ID, UNKNOWN_ID, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}
