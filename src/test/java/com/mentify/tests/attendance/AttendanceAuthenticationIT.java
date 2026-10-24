package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
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
        AttendanceTestDataConfiguration config = AttendanceTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.courseId() != null,
                "Set TEST_ATTENDANCE_COURSE_ID to a valid fixture course");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, attendanceClient.createSession(
                    AttendanceTestDataFactory.session(config.courseId()), student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.listSessions(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.getSession(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.complete(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, attendanceClient.studentSummary(UNKNOWN_ID, UNKNOWN_ID, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCannotListSessionsForAnotherTeachersCourse() {
        AttendanceTestDataConfiguration config = AttendanceTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.courseId() != null,
                "Set TEST_ATTENDANCE_COURSE_ID to a course owned by the primary teacher");
        Assumptions.assumeTrue(hasSecondaryTeacherCredentials(),
                "Set distinct SECONDARY_TEACHER_TEST_USERNAME/PASSWORD credentials");

        AuthenticationSession otherTeacher = login(TestRole.SECONDARY_TEACHER);
        try {
            assertEquals(403, attendanceClient.listSessions(config.courseId().toString(),
                    otherTeacher.accessToken()).statusCode());
        } finally {
            logout(otherTeacher);
        }
    }

    private static boolean hasSecondaryTeacherCredentials() {
        String username = System.getenv("SECONDARY_TEACHER_TEST_USERNAME");
        String password = System.getenv("SECONDARY_TEACHER_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
