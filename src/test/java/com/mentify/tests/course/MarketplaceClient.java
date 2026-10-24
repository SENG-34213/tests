package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MarketplaceClient {

    private static final String PUBLIC_PATH = "/api/v1/marketplace/materials";
    private static final String ADMIN_PATH = "/api/v1/marketplace/admin/products";
    private final ApiClient apiClient;

    public MarketplaceClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response listPublic(String search, String subject, String gradeId, int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (search != null) query.put("search", search);
        if (subject != null) query.put("subject", subject);
        if (gradeId != null) query.put("gradeId", gradeId);
        query.put("page", page);
        query.put("size", size);
        return apiClient.get(PUBLIC_PATH, null, query, Map.of());
    }

    public Response getPublic(String productId) {
        return apiClient.get(PUBLIC_PATH + "/" + required(productId, "productId"), null, Map.of(), Map.of());
    }

    public Response listAdmin(String search, int page, int size, String token) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (search != null) query.put("search", search);
        query.put("page", page);
        query.put("size", size);
        return apiClient.get(ADMIN_PATH, token, query, Map.of());
    }

    public Response create(Object request, String token) {
        return apiClient.post(ADMIN_PATH, request, token);
    }

    public Response update(String productId, Object request, String token) {
        return apiClient.put(productPath(productId), request, token);
    }

    public Response changeStatus(String productId, String status, String token) {
        return apiClient.patch(productPath(productId) + "/status", null, token,
                Map.of("status", required(status, "status")), Map.of());
    }

    private static String productPath(String productId) {
        return ADMIN_PATH + "/" + required(productId, "productId");
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
