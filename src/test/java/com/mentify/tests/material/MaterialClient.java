package com.mentify.tests.material;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class MaterialClient {

    private static final String API_PATH = "/api/v1/materials";

    private final ApiClient apiClient;

    public MaterialClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response upload(Map<String, ?> fields, File file, String token) {
        return apiClient.postMultipart(API_PATH, fields, file, token);
    }

    public Response uploadWithoutFile(Map<String, ?> fields, String token) {
        return apiClient.postMultipart(API_PATH, fields, null, token);
    }

    public Response uploadMarketplaceMaterial(String title, String description, File file, String token) {
        Map<String, Object> fields = new java.util.LinkedHashMap<>();
        fields.put("title", required(title, "title"));
        if (description != null) fields.put("description", description);
        return apiClient.postMultipart(API_PATH + "/marketplace", fields,
                Objects.requireNonNull(file, "file must not be null"), token);
    }

    public Response uploadProfileImageWithoutFile(String token) {
        return apiClient.postMultipart("/api/v1/profile-images", Map.of(), null, token);
    }

    public Response uploadProfileImage(File file, String token) {
        return apiClient.postMultipart("/api/v1/profile-images", Map.of(),
                Objects.requireNonNull(file, "file must not be null"), token);
    }

    public Response downloadProfileImage(String key, String token) {
        return apiClient.get("/api/v1/profile-images", token, Map.of("key", required(key, "key")), Map.of());
    }

    public Response list(Map<String, ?> queryParameters, String token) {
        return apiClient.get(API_PATH, token, queryParameters, Map.of());
    }

    public Response aiSources(String courseId, String token) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId must not be blank");
        }
        return apiClient.get(API_PATH + "/ai-sources", token, Map.of("courseId", courseId), Map.of());
    }

    public Response get(String materialId, String token) {
        return apiClient.get(materialPath(materialId), token, Map.of(), Map.of());
    }

    public Response download(String materialId, String token) {
        return apiClient.get(materialPath(materialId) + "/download", token, Map.of(), Map.of());
    }

    public Response update(String materialId, Object request, String token) {
        return apiClient.put(materialPath(materialId), request, token);
    }

    public Response archive(String materialId, String token) {
        return apiClient.post(materialPath(materialId) + "/archive", null, token);
    }

    public Response delete(String materialId, String token) {
        return apiClient.delete(materialPath(materialId), token);
    }

    public Response audit(Object request, String token) {
        return apiClient.post(API_PATH + "/maintenance/audit", request, token);
    }

    public Response cleanup(Object request, String token) {
        return apiClient.post(API_PATH + "/maintenance/cleanup", request, token);
    }

    private static String materialPath(String materialId) {
        if (materialId == null || materialId.isBlank()) {
            throw new IllegalArgumentException("materialId must not be blank");
        }
        return API_PATH + "/" + materialId;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
