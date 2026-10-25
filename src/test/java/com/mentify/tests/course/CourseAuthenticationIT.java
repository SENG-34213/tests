package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseAuthenticationIT extends CourseIntegrationTestSupport {

    private static final String UNKNOWN_COURSE_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void shouldRejectCourseListWithoutAuthentication() {
        Response response = courseClient.getAllCourses(null);

        assertEquals(401, response.statusCode(), response.asString());
    }

    @Test
    void shouldRejectTeacherCoursesWithoutAuthentication() {
        Response response = courseClient.myCourses(null);

        assertEquals(401, response.statusCode(), response.asString());
    }

    @Test
    void shouldAllowTeacherToReadAssignedCourses() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            Response response = courseClient.myCourses(session.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(200, response.jsonPath().getInt("statusCode"), response.asString());
            assertNotNull(response.jsonPath().get("data"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowStudentToUseCourseLookup() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = courseClient.lookup(UNKNOWN_COURSE_ID, session.accessToken());

            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowAdminToReadAllCourses() {
        AuthenticationSession session = login(TestRole.ADMIN);

        try {
            Response response = courseClient.getAllCourses(session.accessToken());

            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(200, response.jsonPath().getInt("statusCode"), response.asString());
            assertNotNull(response.jsonPath().get("data"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentFromReadingAllCourses() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = courseClient.getAllCourses(session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectTeacherFromReadingAllCourses() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            Response response = courseClient.getAllCourses(session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentFromCreatingACourse() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = courseClient.create(validCourseRequest(), session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowTeacherRoleToReachCourseCreation() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            Response profile = apiClient.get("/api/v1/users/me", session.accessToken(), Map.of(), Map.of());
            assertEquals(200, profile.statusCode(), profile.asString());
            String teacherId = profile.jsonPath().getString("data.id");
            assertNotNull(teacherId, profile.asString());

            Response response = courseClient.create(validCourseRequest(teacherId), session.accessToken());

            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentFromDeletingACourse() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = courseClient.delete(UNKNOWN_COURSE_ID, session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectStudentFromBulkCourseLookup() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = courseClient.bulkLookup(
                    Map.of("ids", List.of(UNKNOWN_COURSE_ID)), session.accessToken());

            assertEquals(403, response.statusCode(), response.asString());
        } finally {
            logout(session);
        }
    }

    private static Map<String, Object> validCourseRequest() {
        return Map.of(
                "courseName", "TEST-008 Course " + UUID.randomUUID(),
                "courseDescription", "Authorization-only request; it must be rejected before persistence.",
                "courseFeeMonthly", 25.00,
                "gradeId", UNKNOWN_COURSE_ID,
                "assignedTeacherId", UNKNOWN_COURSE_ID,
                "subject", "Integration Testing");
    }

    private static Map<String, Object> validCourseRequest(String assignedTeacherId) {
        Map<String, Object> request = new java.util.LinkedHashMap<>(validCourseRequest());
        request.put("assignedTeacherId", assignedTeacherId);
        return request;
    }
}
