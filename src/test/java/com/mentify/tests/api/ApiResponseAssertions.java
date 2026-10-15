package com.mentify.tests.api;

import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ApiResponseAssertions {

    private ApiResponseAssertions() {
    }

    public static void assertStatus(Response response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(), response.asString());
    }

    public static void assertContentTypeJson(Response response) {
        String contentType = response.contentType();
        assertTrue(contentType != null && contentType.toLowerCase().contains("json"),
                "Expected JSON content type but received: " + contentType);
    }

    public static void assertBodyNotEmpty(Response response) {
        assertFalse(response.asString().isBlank(), "Expected a non-empty response body");
    }

    public static void assertJsonField(Response response, String field) {
        assertTrue(response.jsonPath().get(field) != null,
                "Expected JSON field to exist: " + field);
    }

    public static void assertJsonValue(Response response, String field, Object expectedValue) {
        assertEquals(expectedValue, response.jsonPath().get(field),
                "Unexpected JSON value for field: " + field);
    }
}
