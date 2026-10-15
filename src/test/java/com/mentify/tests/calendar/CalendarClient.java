package com.mentify.tests.calendar;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class CalendarClient {

    private static final String UPCOMING_PATH = "/api/v1/calendar/upcoming";
    private static final String EVENTS_PATH = "/api/v1/calendar/events";

    private final ApiClient apiClient;

    public CalendarClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response upcoming(String bearerToken, int limit) {
        return apiClient.get(UPCOMING_PATH, bearerToken, Map.of("limit", limit), Map.of());
    }

    public Response create(Object request, String bearerToken) {
        return apiClient.post(EVENTS_PATH, request, bearerToken);
    }
}
