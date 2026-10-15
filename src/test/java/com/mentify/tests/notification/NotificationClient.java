package com.mentify.tests.notification;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class NotificationClient {

    private static final String PATH = "/api/v1/notifications";
    private final ApiClient apiClient;

    public NotificationClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response list(int page, int size, boolean unreadOnly, String token) {
        validatePage(page, size);
        return apiClient.get(PATH, token, Map.of("page", page, "size", size, "unreadOnly", unreadOnly), Map.of());
    }

    public Response unreadCount(String token) {
        return apiClient.get(PATH + "/unread-count", token, Map.of(), Map.of());
    }

    public Response markRead(String notificationId, String token) {
        return apiClient.patch(PATH + "/" + id(notificationId) + "/read", null, token);
    }

    public Response markAllRead(String token) {
        return apiClient.patch(PATH + "/read-all", null, token);
    }

    public Response delete(String notificationId, String token) {
        return apiClient.delete(PATH + "/" + id(notificationId), token);
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
    }

    private static String id(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("notificationId must not be blank");
        }
        return value;
    }
}
