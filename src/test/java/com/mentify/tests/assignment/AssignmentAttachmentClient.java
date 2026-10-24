package com.mentify.tests.assignment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class AssignmentAttachmentClient {

    private final ApiClient apiClient;

    public AssignmentAttachmentClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(String assignmentId, Object request, String token) {
        return apiClient.post(assignmentPath(assignmentId) + "/attachments", request, token);
    }

    public Response upload(String assignmentId, File file, String token) {
        return apiClient.postMultipart(assignmentPath(assignmentId) + "/attachments/upload",
                Map.of(), Objects.requireNonNull(file, "file must not be null"), token);
    }

    public Response list(String assignmentId, String token) {
        return apiClient.get(assignmentPath(assignmentId) + "/attachments", token, Map.of(), Map.of());
    }

    public Response download(String assignmentId, String attachmentId, String token) {
        return apiClient.get(downloadPath(assignmentId, attachmentId), token, Map.of(), Map.of());
    }

    public Response delete(String assignmentId, String attachmentId, String token) {
        return apiClient.delete(attachmentPath(assignmentId, attachmentId), token);
    }

    private static String assignmentPath(String assignmentId) {
        return pathWithId("assignmentId", "/api/v1/assignments/", assignmentId);
    }

    private static String attachmentPath(String assignmentId, String attachmentId) {
        return assignmentPath(assignmentId) + "/attachments/" + required("attachmentId", attachmentId);
    }

    private static String downloadPath(String assignmentId, String attachmentId) {
        return attachmentPath(assignmentId, attachmentId) + "/download";
    }

    private static String pathWithId(String name, String prefix, String value) {
        return prefix + required(name, value);
    }

    private static String required(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
