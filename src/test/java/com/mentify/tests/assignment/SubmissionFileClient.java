package com.mentify.tests.assignment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class SubmissionFileClient {

    private final ApiClient apiClient;

    public SubmissionFileClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(String submissionId, Object request, String token) {
        return apiClient.post(submissionPath(submissionId) + "/files", request, token);
    }

    public Response upload(String submissionId, File file, String token) {
        return apiClient.postMultipart(submissionPath(submissionId) + "/files/upload",
                Map.of(), Objects.requireNonNull(file, "file must not be null"), token);
    }

    public Response list(String submissionId, String token) {
        return apiClient.get(submissionPath(submissionId) + "/files", token, Map.of(), Map.of());
    }

    public Response download(String submissionId, String fileId, String token) {
        return apiClient.get(downloadPath(submissionId, fileId), token, Map.of(), Map.of());
    }

    public Response delete(String submissionId, String fileId, String token) {
        return apiClient.delete(filePath(submissionId, fileId), token);
    }

    private static String submissionPath(String submissionId) {
        return pathWithId("submissionId", "/api/v1/submissions/", submissionId);
    }

    private static String filePath(String submissionId, String fileId) {
        return submissionPath(submissionId) + "/files/" + required("fileId", fileId);
    }

    private static String downloadPath(String submissionId, String fileId) {
        return filePath(submissionId, fileId) + "/download";
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
