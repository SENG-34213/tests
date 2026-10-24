package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.api.HttpMethod;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Role regression checks for course-service routes that must not mutate as a student. */
class CourseServiceAuthorizationMatrixIT extends CourseIntegrationTestSupport {

    private static final String ZERO = "00000000-0000-0000-0000-000000000000";
    private static final String COURSE = "/api/v1/course/" + ZERO;
    private static final String MODULES = COURSE + "/modules";
    private static final String GRADE = "/api/v1/course/grades/" + ZERO;

    @Test
    void shouldDenyStudentAllCourseContentMutations() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            List<DeniedRequest> requests = List.of(
                    new DeniedRequest(HttpMethod.POST, MODULES, "{}"),
                    new DeniedRequest(HttpMethod.PUT, MODULES + "/" + ZERO, "{}"),
                    new DeniedRequest(HttpMethod.DELETE, MODULES + "/" + ZERO, null),
                    new DeniedRequest(HttpMethod.POST, MODULES + "/" + ZERO + "/lessons", "{}"),
                    new DeniedRequest(HttpMethod.PUT, MODULES + "/" + ZERO + "/lessons/" + ZERO, "{}"),
                    new DeniedRequest(HttpMethod.DELETE, MODULES + "/" + ZERO + "/lessons/" + ZERO, null),
                    new DeniedRequest(HttpMethod.POST, MODULES + "/" + ZERO + "/learning-materials", "{}"),
                    new DeniedRequest(HttpMethod.PUT,
                            MODULES + "/" + ZERO + "/learning-materials/" + ZERO, "{}"),
                    new DeniedRequest(HttpMethod.DELETE,
                            MODULES + "/" + ZERO + "/learning-materials/" + ZERO, null));

            for (DeniedRequest request : requests) {
                Response response = apiClient.requestResponse(request.method(), request.path(), request.body(),
                        student.accessToken(), java.util.Map.of(), java.util.Map.of());
                assertEquals(403, response.statusCode(), request.method() + " " + request.path()
                        + " should be forbidden for students; " + response.asString());
            }
            assertEquals(403, apiClient.postMultipart(MODULES + "/" + ZERO + "/learning-materials/upload",
                    java.util.Map.of("title", "blocked"), null, student.accessToken()).statusCode());
            assertEquals(403, apiClient.postMultipart(MODULES + "/" + ZERO + "/lessons/" + ZERO
                    + "/materials/upload", java.util.Map.of("title", "blocked"), null,
                    student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void shouldRestrictGradeAdministrationToAdministrators() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            List<DeniedRequest> requests = List.of(
                    new DeniedRequest(HttpMethod.GET, "/api/v1/course/grades", null),
                    new DeniedRequest(HttpMethod.POST, "/api/v1/course/grades", "{}"),
                    new DeniedRequest(HttpMethod.GET, GRADE, null),
                    new DeniedRequest(HttpMethod.PUT, GRADE, "{}"),
                    new DeniedRequest(HttpMethod.DELETE, GRADE, null),
                    new DeniedRequest(HttpMethod.GET, "/api/v1/course/grades/monthly-fees", null),
                    new DeniedRequest(HttpMethod.GET, GRADE + "/monthly-fee", null),
                    new DeniedRequest(HttpMethod.POST, GRADE + "/monthly-fee", "{}"),
                    new DeniedRequest(HttpMethod.PUT, GRADE + "/monthly-fee", "{}"),
                    new DeniedRequest(HttpMethod.DELETE, GRADE + "/monthly-fee", null));

            for (DeniedRequest request : requests) {
                Response response = apiClient.requestResponse(request.method(), request.path(), request.body(),
                        teacher.accessToken(), java.util.Map.of(), java.util.Map.of());
                assertEquals(403, response.statusCode(), request.method() + " " + request.path()
                        + " should be forbidden for teachers; " + response.asString());
            }
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldRequireAuthenticationForCourseContentAndGradeAdministration() {
        List<DeniedRequest> requests = List.of(
                new DeniedRequest(HttpMethod.GET, MODULES, null),
                new DeniedRequest(HttpMethod.POST, MODULES, "{}"),
                new DeniedRequest(HttpMethod.GET, MODULES + "/" + ZERO, null),
                new DeniedRequest(HttpMethod.GET, MODULES + "/" + ZERO + "/lessons", null),
                new DeniedRequest(HttpMethod.GET, MODULES + "/" + ZERO + "/learning-materials", null),
                new DeniedRequest(HttpMethod.GET, "/api/v1/course/grades", null),
                new DeniedRequest(HttpMethod.GET, "/api/v1/course/grades/monthly-fees", null));

        for (DeniedRequest request : requests) {
            Response response = apiClient.requestResponse(request.method(), request.path(), request.body(),
                    null, java.util.Map.of(), java.util.Map.of());
            assertEquals(401, response.statusCode(), request.method() + " " + request.path()
                    + " should require authentication; " + response.asString());
        }
        assertEquals(401, apiClient.get("/api/v1/teacher/students", null,
                java.util.Map.of("courseId", ZERO), java.util.Map.of()).statusCode());
        assertEquals(401, apiClient.get("/api/v1/teacher/students/" + ZERO + "/summary", null,
                java.util.Map.of("courseId", ZERO), java.util.Map.of()).statusCode());
        assertEquals(401, apiClient.get("/api/v1/teacher/gradebook", null,
                java.util.Map.of("courseId", ZERO), java.util.Map.of()).statusCode());
        assertEquals(401, apiClient.get("/api/v1/teacher/analytics", null,
                java.util.Map.of("courseId", ZERO), java.util.Map.of()).statusCode());
    }

    @Test
    void shouldKeepStudentProgressAndAnnouncementsStudentOnly() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response announcements = apiClient.get("/api/v1/announcements/me", student.accessToken(),
                    java.util.Map.of(), java.util.Map.of());
            assertEquals(200, announcements.statusCode(), announcements.asString());

            Response progress = apiClient.get("/api/v1/progress/me", student.accessToken(),
                    java.util.Map.of(), java.util.Map.of());
            assertEquals(200, progress.statusCode(), progress.asString());
            assertEquals("success", progress.jsonPath().getString("status"));
            assertNotNull(progress.jsonPath().get("data"), progress.asString());
        } finally {
            logout(student);
        }

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response announcements = apiClient.get("/api/v1/announcements/me", teacher.accessToken(),
                    java.util.Map.of(), java.util.Map.of());
            assertEquals(403, announcements.statusCode(), announcements.asString());

            Response progress = apiClient.get("/api/v1/progress/me", teacher.accessToken(),
                    java.util.Map.of(), java.util.Map.of());
            assertEquals(403, progress.statusCode(), progress.asString());
        } finally {
            logout(teacher);
        }
    }

    private record DeniedRequest(HttpMethod method, String path, Object body) { }
}
