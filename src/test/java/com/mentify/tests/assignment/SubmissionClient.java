package com.mentify.tests.assignment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class SubmissionClient {

    private final ApiClient apiClient;

    public SubmissionClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(String assignmentId, Object request, String token) {
        return apiClient.post(assignmentPath(assignmentId) + "/submissions", request, token);
    }

    public Response update(String submissionId, Object request, String token) {
        return apiClient.put(submissionPath(submissionId), request, token);
    }

    public Response submit(String submissionId, String token) {
        return apiClient.post(submissionPath(submissionId) + "/submit", null, token);
    }

    public Response get(String submissionId, String token) {
        return apiClient.get(submissionPath(submissionId), token, Map.of(), Map.of());
    }

    public Response listForAssignment(String assignmentId, String token) {
        return apiClient.get(assignmentPath(assignmentId) + "/submissions", token, Map.of(), Map.of());
    }

    public Response listMine(String token) {
        return apiClient.get("/api/v1/submissions/my", token, Map.of(), Map.of());
    }

    public Response grade(String submissionId, Object request, String token) {
        return apiClient.post(submissionPath(submissionId) + "/grade", request, token);
    }

    public Response returnSubmission(String submissionId, String token) {
        return apiClient.post(submissionPath(submissionId) + "/return", null, token);
    }

    private static String assignmentPath(String assignmentId) {
        return pathWithId("assignmentId", "/api/v1/assignments/", assignmentId);
    }

    private static String submissionPath(String submissionId) {
        return pathWithId("submissionId", "/api/v1/submissions/", submissionId);
    }

    private static String pathWithId(String name, String prefix, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return prefix + value;
    }
}
