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

    public Response delete(String courseId, String bearerToken) {
        return apiClient.delete(coursePath(courseId), bearerToken);
    }

    private static String coursePath(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId must not be blank");
        }
        return COURSE_PATH + "/" + courseId;
    }
}
