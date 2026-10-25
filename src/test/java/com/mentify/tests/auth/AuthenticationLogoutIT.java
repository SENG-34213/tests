package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AuthenticationLogoutIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldClearTheSessionAfterSuccessfulLogout() {
        AuthenticationSession session = login(TestRole.STUDENT);
        Assumptions.assumeTrue(session.refreshable(),
                "Logout integration assertions require a refresh token in the login response");

        Response response = authenticationClient.logoutResponse(session.refreshToken());
        assertEquals(200, response.statusCode(), response.asString());
        session.clear();

        assertFalse(session.authenticated());
        assertFalse(session.refreshable());
    }
}
