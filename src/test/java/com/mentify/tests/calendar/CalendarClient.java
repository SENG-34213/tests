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

    public Response events(String bearerToken) {
        return apiClient.get(EVENTS_PATH, bearerToken, Map.of(), Map.of());
    }

    public Response create(Object request, String bearerToken) {
        return apiClient.post(EVENTS_PATH, request, bearerToken);
    }

    public Response update(String eventId, Object request, String bearerToken) {
        return apiClient.put(eventPath(eventId), request, bearerToken);
    }

    public Response cancel(String eventId, String bearerToken) {
        return apiClient.post(eventPath(eventId) + "/cancel", null, bearerToken);
    }

    private static String eventPath(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        return EVENTS_PATH + "/" + eventId;
    }
}
