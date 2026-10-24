package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationLoginIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldAuthenticateAStudentWithValidCredentials() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            assertTrue(session.authenticated());
            assertFalse(session.accessToken().isBlank());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectAnInvalidPassword() {
        TestCredentials credentials = authenticationConfiguration.credentialsFor(TestRole.STUDENT);
        Response response = post("/api/v1/auth/login", Map.of(
                "identifier", credentials.username(),
                "password", "invalid-password-for-test"));

        assertEquals(400, response.statusCode());
        assertFalse(response.asString().contains("invalid-password-for-test"));
    }

    @Test
    void shouldRejectAnUnknownUser() {
        Response response = post("/api/v1/auth/login", Map.of(
                "identifier", "unknown-user-" + System.nanoTime() + "@example.invalid",
                "password", "invalid-password-for-test"));

        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRejectAnInvalidLoginRequest() {
        Response response = post("/api/v1/auth/login", Map.of(
                "identifier", "not-an-email",
                "password", ""));

        assertEquals(400, response.statusCode());
    }
}
