package com.mentify.tests.assignment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class AssignmentClient {

    private static final String API_PATH = "/api/v1";

    private final ApiClient apiClient;

    public AssignmentClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(Object request, String token) {
        return apiClient.post(API_PATH + "/assignments", request, token);
    }

    public Response update(String assignmentId, Object request, String token) {
        return apiClient.put(assignmentPath(assignmentId), request, token);
    }

    public Response get(String assignmentId, String token) {
        return apiClient.get(assignmentPath(assignmentId), token, Map.of(), Map.of());
    }

    public Response listForCourse(String courseId, String token) {
        return apiClient.get(coursePath(courseId), token, Map.of(), Map.of());
    }

    public Response delete(String assignmentId, String token) {
        return apiClient.delete(assignmentPath(assignmentId), token);
    }

    public Response publish(String assignmentId, String token) {
        return apiClient.post(assignmentPath(assignmentId) + "/publish", null, token);
    }

    public Response close(String assignmentId, String token) {
        return apiClient.post(assignmentPath(assignmentId) + "/close", null, token);
    }

    private static String assignmentPath(String assignmentId) {
        return pathWithId("assignmentId", "/api/v1/assignments/", assignmentId);
    }

    private static String coursePath(String courseId) {
        return pathWithId("courseId", "/api/v1/courses/", courseId) + "/assignments";
    }

    private static String pathWithId(String name, String prefix, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return prefix + value;
    }
}
