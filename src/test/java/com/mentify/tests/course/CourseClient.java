package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class CourseClient {

    private static final String COURSE_PATH = "/api/v1/course";

    private final ApiClient apiClient;

    public CourseClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response myCourses(String bearerToken) {
        return apiClient.get(COURSE_PATH + "/my-courses", bearerToken, Map.of(), Map.of());
    }

    public Response getAllCourses(String bearerToken) {
        return apiClient.get(COURSE_PATH, bearerToken, Map.of(), Map.of());
    }

    public Response lookup(String courseId, String bearerToken) {
        return apiClient.get(coursePath(courseId) + "/lookup", bearerToken, Map.of(), Map.of());
    }

    public Response getById(String courseId, String bearerToken) {
        return apiClient.get(coursePath(courseId), bearerToken, Map.of(), Map.of());
    }

    public Response create(Object request, String bearerToken) {
        return apiClient.post(COURSE_PATH, request, bearerToken);
    }

    public Response update(String courseId, Object request, String bearerToken) {
        return apiClient.put(coursePath(courseId), request, bearerToken);
    }

    public Response bulkLookup(Object request, String bearerToken) {
        return apiClient.post(COURSE_PATH + "/bulk", request, bearerToken);
    }

    public Response teacherDashboard(String bearerToken) {
        return apiClient.get("/api/v1/teacher/dashboard", bearerToken, Map.of(), Map.of());
    }

    public Response teacherSearch(String query, String bearerToken) {
        return teacherSearch(query, null, null, bearerToken);
    }

    public Response teacherSearch(String query, String types, Integer limit, String bearerToken) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        Map<String, Object> queryParameters = new java.util.LinkedHashMap<>();
        queryParameters.put("q", query);
        if (types != null) {
            queryParameters.put("types", types);
        }
        if (limit != null) {
            queryParameters.put("limit", limit);
        }
        return apiClient.get("/api/v1/teacher/search", bearerToken, queryParameters, Map.of());
    }

    public Response teacherStudents(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/students", bearerToken,
                Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherStudentSummary(String studentId, String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/students/" + requiredId(studentId, "studentId") + "/summary",
                bearerToken, Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherGradebook(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/gradebook", bearerToken,
                Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherAnalytics(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/analytics", bearerToken,
                Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response delete(String courseId, String bearerToken) {
        return apiClient.delete(coursePath(courseId), bearerToken);
    }

    private static String coursePath(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId must not be blank");
        }
        return COURSE_PATH + "/" + courseId;
    }

    private static String requiredId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
