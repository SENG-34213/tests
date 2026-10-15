package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationConfigurationTest {

    @Test
    void shouldRequireTheGenericTestCredentialsWhenLoginIsRequested() {
        AuthenticationConfiguration configuration = AuthenticationConfiguration.fromEnvironment(Map.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                configuration::requireDefaultCredentials);

        assertTrue(exception.getMessage().contains("TEST_USERNAME"));
    }

    @Test
    void shouldReadRoleSpecificCredentialsAndHidePasswords() {
        AuthenticationConfiguration configuration = AuthenticationConfiguration.fromEnvironment(Map.of(
                "STUDENT_TEST_USERNAME", "student@example.test",
                "STUDENT_TEST_PASSWORD", "secret"));

        assertEquals("student@example.test", configuration.credentialsFor(TestRole.STUDENT).username());
        assertTrue(configuration.credentialsFor(TestRole.STUDENT).toString().contains("[REDACTED]"));
        assertTrue(!configuration.credentialsFor(TestRole.STUDENT).toString().contains("secret"));
    }

    @Test
    void shouldRejectIncompleteCredentialPairs() {
        assertThrows(IllegalArgumentException.class, () -> AuthenticationConfiguration.fromEnvironment(Map.of(
                "TEST_USERNAME", "student@example.test")));
    }
}
