package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthenticationRefreshIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldRefreshAValidAuthenticationSession() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            AuthenticationSession refreshed = authenticationClient.refresh(session);

            assertTrue(refreshed.authenticated());
            assertTrue(refreshed.accessToken() != null && !refreshed.accessToken().isBlank());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectAnInvalidRefreshToken() {
        AuthenticationSession invalidSession = new AuthenticationSession(
                "invalid-access-token", "invalid-refresh-token", "Bearer", 300, 1800,
                "openid", Instant.now());

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationClient.refresh(invalidSession));

        assertEquals(400, exception.statusCode());
    }
}
