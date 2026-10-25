package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.user.UserClient;
import org.junit.jupiter.api.Test;

class AttendanceSummaryIT extends AttendanceIntegrationTestSupport {

    @Test
    void studentCanReadOwnCourseSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.fixturesConfigured(), "Set approved attendance course and student IDs");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            io.restassured.response.Response response = attendanceClient.mySummary(
                    data.courseId().toString(), student.accessToken());
            assertEquals(200, response.statusCode());
            assertSummaryContract(response, data.courseId().toString());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCanReadStudentCourseSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.fixturesConfigured(), "Set approved attendance course and student IDs");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            io.restassured.response.Response response = attendanceClient.studentSummary(
                    data.studentId().toString(), data.courseId().toString(), teacher.accessToken());
            assertEquals(200, response.statusCode());
            assertEquals(data.studentId().toString(), response.jsonPath().getString("data.studentId"));
            assertSummaryContract(response, data.courseId().toString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void studentWithNoAttendanceRecordsReceivesZeroSummary() {
        AttendanceTestDataConfiguration data = AttendanceTestDataConfiguration.fromEnvironment();
        assumeTrue(data.courseId() != null && data.emptySummaryStudentId() != null,
                "Set TEST_ATTENDANCE_COURSE_ID and TEST_ATTENDANCE_EMPTY_SUMMARY_STUDENT_ID for a student with no records");
        requireAdditionalService(RequiredService.USER_SERVICE);
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            var profile = new UserClient(apiClient).currentUser(student.accessToken());
            assertEquals(200, profile.statusCode(), profile.asString());
            assertEquals(data.emptySummaryStudentId().toString(), profile.jsonPath().getString("data.id"),
                    profile.asString());

            var response = attendanceClient.studentSummary(data.emptySummaryStudentId().toString(),
                    data.courseId().toString(), student.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(data.emptySummaryStudentId().toString(), response.jsonPath().getString("data.studentId"));
            assertEquals(data.courseId().toString(), response.jsonPath().getString("data.courseId"));
            assertEquals(0, response.jsonPath().getLong("data.completedSessions"));
            assertEquals(0, response.jsonPath().getLong("data.presentCount"));
            assertEquals(0, response.jsonPath().getLong("data.lateCount"));
            assertEquals(0, response.jsonPath().getLong("data.absentCount"));
            assertEquals(0, response.jsonPath().getLong("data.attendedCount"));
            assertEquals(0.0, response.jsonPath().getDouble("data.attendancePercentage"), 0.001);
        } finally {
            logout(student);
        }
    }

    private static void assertSummaryContract(io.restassured.response.Response response, String courseId) {
        long completed = response.jsonPath().getLong("data.completedSessions");
        long present = response.jsonPath().getLong("data.presentCount");
        long late = response.jsonPath().getLong("data.lateCount");
        long absent = response.jsonPath().getLong("data.absentCount");
        long attended = response.jsonPath().getLong("data.attendedCount");
        double percentage = response.jsonPath().getDouble("data.attendancePercentage");

        assertEquals(courseId, response.jsonPath().getString("data.courseId"));
        assertTrue(completed >= 0 && present >= 0 && late >= 0 && absent >= 0 && attended >= 0);
        assertEquals(completed, present + late + absent);
        assertEquals(present + late, attended);
        double expectedPercentage = completed == 0 ? 0.0 : Math.round(attended * 10000.0 / completed) / 100.0;
        assertEquals(expectedPercentage, percentage, 0.001);
        assertTrue(percentage >= 0.0 && percentage <= 100.0);
    }
}
