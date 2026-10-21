package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
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
