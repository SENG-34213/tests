package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
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
                    Map.of("records", java.util.List.of(Map.of("status", "PRESENT"))),
                    teacher.accessToken()).statusCode());
            assertEquals(400, attendanceClient.markAttendance(UNKNOWN_ID,
                    Map.of("records", java.util.List.of(Map.of("studentId", UNKNOWN_ID))),
                    teacher.accessToken()).statusCode());
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

    @Test
    void rejectsOverlongSessionTitleAndAttendanceRemarks() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.courseId() != null,
                "Set TEST_ATTENDANCE_COURSE_ID to a valid non-production course");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Map<String, Object> overlongTitle = new HashMap<>(AttendanceTestDataFactory.session(data.courseId()));
            overlongTitle.put("title", "T".repeat(151));
            assertEquals(400, attendanceClient.createSession(overlongTitle, teacher.accessToken()).statusCode());

            Map<String, Object> overlongRemarks = Map.of("records", java.util.List.of(Map.of(
                    "studentId", UUID.randomUUID().toString(),
                    "status", "PRESENT",
                    "remarks", "R".repeat(501))));
            assertEquals(400, attendanceClient.markAttendance(UNKNOWN_ID, overlongRemarks,
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
