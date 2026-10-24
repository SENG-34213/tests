package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class AnalyticsClient {

    private final ApiClient apiClient;

    public AnalyticsClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response adminOverview(String token) {
        return apiClient.get("/api/v1/analytics/admin/overview", token, Map.of(), Map.of());
    }
}
