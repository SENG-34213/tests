package com.mentify.tests.auth;

import io.restassured.response.Response;
import java.time.Instant;

public final class AuthenticationResponseParser {

    private AuthenticationResponseParser() {
    }

    public static AuthenticationSession successfulSession(Response response) {
        String accessToken = response.jsonPath().getString("data.accessToken");
        if (response.statusCode() < 200 || response.statusCode() >= 300 || accessToken == null
                || accessToken.isBlank()) {
            throw failure(response);
        }
        return new AuthenticationSession(
                accessToken,
                response.jsonPath().getString("data.refreshToken"),
                response.jsonPath().getString("data.tokenType"),
                response.jsonPath().getInt("data.expiresIn"),
                response.jsonPath().getInt("data.refreshExpiresIn"),
                response.jsonPath().getString("data.scope"),
                parseInstant(response.jsonPath().getString("data.issuedAt")));
    }

    public static AuthenticationException failure(Response response) {
        String message = response.jsonPath().getString("message");
        if (message == null || message.isBlank()) {
            message = "Authentication request failed";
        }
        return new AuthenticationException(message, response.statusCode());
    }

    private static Instant parseInstant(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }
}
