package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class AuthenticationRecoveryIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldReturnGenericResultForUnknownPasswordResetAccount() {
        String unknownEmail = "missing-" + System.nanoTime() + "@example.invalid";

        Response response = authenticationClient.requestPasswordReset(unknownEmail);

        assertEquals(200, response.statusCode(), response.asString());
        assertTrue(response.asString().contains("If an account exists"), response.asString());
        assertFalse(response.asString().contains(unknownEmail),
                "Password reset response must not disclose the supplied email");
    }

    @Test
    void shouldRejectInvalidPasswordResetOtpAndToken() {
        Response verify = authenticationClient.verifyPasswordReset(
                "unknown-" + System.nanoTime() + "@example.invalid", "000000");
        assertEquals(400, verify.statusCode(), verify.asString());

        Response reset = authenticationClient.resetPassword("not-a-reset-token", "NewPassword123!");
        assertEquals(400, reset.statusCode(), reset.asString());
    }

    @Test
    void shouldExposePublicJwksDocument() {
        Response response = authenticationClient.jwks();

        assertEquals(200, response.statusCode(), response.asString());
        assertTrue(response.jsonPath().getList("keys") != null, response.asString());
    }
}
