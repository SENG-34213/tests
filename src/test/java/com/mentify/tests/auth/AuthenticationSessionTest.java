package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthenticationSessionTest {

    @Test
    void shouldClearTokensWithoutExposingThemInDiagnostics() {
        AuthenticationSession session = new AuthenticationSession(
                "access-secret", "refresh-secret", "Bearer", 300, 1800,
                "openid", Instant.parse("2026-01-01T00:00:00Z"));

        assertTrue(session.authenticated());
        session.clear();

        assertFalse(session.authenticated());
        assertFalse(session.refreshable());
        assertFalse(session.toString().contains("secret"));
    }
}
