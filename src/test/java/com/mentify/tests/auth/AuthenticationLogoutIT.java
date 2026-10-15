package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AuthenticationLogoutIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldClearTheSessionAfterSuccessfulLogout() {
        AuthenticationSession session = login(TestRole.STUDENT);
        Assumptions.assumeTrue(session.refreshable(),
                "Logout integration assertions require a refresh token in the login response");

        authenticationClient.logout(session);

        assertFalse(session.authenticated());
        assertFalse(session.refreshable());
    }
}
