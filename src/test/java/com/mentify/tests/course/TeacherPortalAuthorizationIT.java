package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class TeacherPortalAuthorizationIT extends CourseIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldAllowTeacherToReadDashboardAndSearch() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response dashboard = courseClient.teacherDashboard(teacher.accessToken());
            assertEquals(200, dashboard.statusCode(), dashboard.asString());
            assertEquals("success", dashboard.jsonPath().getString("status"));
            assertNotNull(dashboard.jsonPath().get("data"), dashboard.asString());

            Response search = courseClient.teacherSearch("mentify", teacher.accessToken());
            assertEquals(200, search.statusCode(), search.asString());
            assertEquals("success", search.jsonPath().getString("status"));
            assertNotNull(search.jsonPath().get("data"), search.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldRejectUnauthenticatedDashboardAndSearch() {
        assertEquals(401, courseClient.teacherDashboard(null).statusCode());
        assertEquals(401, courseClient.teacherSearch("mentify", null).statusCode());
    }

    @Test
    void shouldRejectStudentFromTeacherPortalReads() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, courseClient.teacherDashboard(student.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherSearch("mentify", student.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherStudents(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherStudentSummary(
                    UNKNOWN_ID, UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherGradebook(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, courseClient.teacherAnalytics(UNKNOWN_ID, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}
