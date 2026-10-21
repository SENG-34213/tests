package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.time.LocalDate;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeacherPortalDataIT extends CourseIntegrationTestSupport {

    private TeacherPortalTestData data;

    @BeforeEach
    void requireTeacherPortalFixtures() {
        data = TeacherPortalTestData.fromEnvironment();
        Assumptions.assumeTrue(data.configured(),
                "Set TEST_TEACHER_PORTAL_COURSE_ID and TEST_TEACHER_PORTAL_STUDENT_ID "
                        + "to a course owned by the test teacher and its enrolled student");
    }

    @Test
    void shouldReturnDirectorySummaryGradebookAndAnalyticsForConfiguredCourse() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response directory = courseClient.teacherStudents(data.courseId().toString(), teacher.accessToken());
            assertSuccessfulData(directory);
            assertNotNull(directory.jsonPath().getList("data"), directory.asString());

            Response summary = courseClient.teacherStudentSummary(data.enrolledStudentId().toString(),
                    data.courseId().toString(), teacher.accessToken());
            assertSuccessfulData(summary);
            assertEquals(data.courseId().toString(), summary.jsonPath().getString("data.courseId"));
            assertEquals(data.enrolledStudentId().toString(), summary.jsonPath().getString("data.studentId"));
            assertNotNull(summary.jsonPath().get("data.attendancePercentage"), summary.asString());
            assertNotNull(summary.jsonPath().get("data.overallPercentage"), summary.asString());

            Response gradebook = courseClient.teacherGradebook(data.courseId().toString(), teacher.accessToken());
            assertSuccessfulData(gradebook);
            assertEquals(data.courseId().toString(), gradebook.jsonPath().getString("data.courseId"));
            assertNotNull(gradebook.jsonPath().getMap("data.gradingScale"), gradebook.asString());
            assertNotNull(gradebook.jsonPath().getList("data.students"), gradebook.asString());
            assertNotNull(gradebook.jsonPath().get("data.summary"), gradebook.asString());

            Response analytics = courseClient.teacherAnalytics(data.courseId().toString(), teacher.accessToken());
            assertSuccessfulData(analytics);
            assertEquals(data.courseId().toString(), analytics.jsonPath().getString("data.courseId"));
            assertEquals("TERM", analytics.jsonPath().getString("data.period"));
            assertNotNull(analytics.jsonPath().get("data.summary.studentCount"), analytics.asString());
            assertNotNull(analytics.jsonPath().get("data.attendance"), analytics.asString());
            assertNotNull(analytics.jsonPath().get("data.assignmentPerformance"), analytics.asString());
            assertNotNull(analytics.jsonPath().get("data.quizPerformance"), analytics.asString());
            assertNotNull(analytics.jsonPath().get("data.examPerformance"), analytics.asString());
            assertNotNull(analytics.jsonPath().getList("data.gradeDistribution"), analytics.asString());
            assertNotNull(analytics.jsonPath().getList("data.attendanceCorrelation"), analytics.asString());
            assertNotNull(analytics.jsonPath().getList("data.atRiskStudents"), analytics.asString());
            assertTrue(analytics.jsonPath().getMap("data.summary").containsKey("submissionRate"),
                    analytics.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldDenyTeacherAnalyticsAndGradebookForAnotherTeachersCourse() {
        String otherCourseId = System.getenv("TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID");
        Assumptions.assumeTrue(otherCourseId != null && !otherCourseId.isBlank(),
                "Set TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID to a course owned by a different teacher");

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, courseClient.teacherStudents(otherCourseId, teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherStudentSummary(data.enrolledStudentId().toString(),
                    otherCourseId, teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherGradebook(otherCourseId, teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherAnalytics(otherCourseId, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldValidateAnalyticsPeriodsAndDateRanges() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            LocalDate today = LocalDate.now();
            Response bounded = courseClient.teacherAnalytics(data.courseId().toString(), "WEEK",
                    today.minusDays(6).toString(), today.toString(), teacher.accessToken());
            assertEquals(200, bounded.statusCode(), bounded.asString());
            assertEquals("WEEK", bounded.jsonPath().getString("data.period"));

            Response invalidPeriod = courseClient.teacherAnalytics(data.courseId().toString(), "YEAR",
                    null, null, teacher.accessToken());
            assertEquals(400, invalidPeriod.statusCode(), invalidPeriod.asString());

            Response invertedRange = courseClient.teacherAnalytics(data.courseId().toString(), "MONTH",
                    today.toString(), today.minusDays(1).toString(), teacher.accessToken());
            assertEquals(400, invertedRange.statusCode(), invertedRange.asString());
        } finally {
            logout(teacher);
        }
    }

    private static void assertSuccessfulData(Response response) {
        assertEquals(200, response.statusCode(), response.asString());
        assertEquals("success", response.jsonPath().getString("status"), response.asString());
        assertNotNull(response.jsonPath().get("data"), response.asString());
    }
}
