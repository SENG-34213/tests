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
    void shouldReadSuperAdminCredentialsSeparatelyFromAdmin() {
        AuthenticationConfiguration configuration = AuthenticationConfiguration.fromEnvironment(Map.of(
                "ADMIN_TEST_USERNAME", "admin@example.test",
                "ADMIN_TEST_PASSWORD", "admin-secret",
                "SUPER_ADMIN_TEST_USERNAME", "super@example.test",
                "SUPER_ADMIN_TEST_PASSWORD", "super-secret"));

        assertEquals("admin@example.test", configuration.credentialsFor(TestRole.ADMIN).username());
        assertEquals("super@example.test", configuration.credentialsFor(TestRole.SUPER_ADMIN).username());
        assertTrue(configuration.credentialsFor(TestRole.SUPER_ADMIN).toString().contains("[REDACTED]"));
    }

    @Test
    void shouldSupportASeparateSecondStudentTestAccount() {
        AuthenticationConfiguration configuration = AuthenticationConfiguration.fromEnvironment(Map.of(
                "STUDENT_TEST_USERNAME", "student-one@example.test",
                "STUDENT_TEST_PASSWORD", "student-one-secret",
                "SECONDARY_STUDENT_TEST_USERNAME", "student-two@example.test",
                "SECONDARY_STUDENT_TEST_PASSWORD", "student-two-secret"));

        assertEquals("student-two@example.test",
                configuration.credentialsFor(TestRole.SECONDARY_STUDENT).username());
        assertEquals("student-one@example.test", configuration.credentialsFor(TestRole.STUDENT).username());
    }

    @Test
    void shouldRejectIncompleteCredentialPairs() {
        assertThrows(IllegalArgumentException.class, () -> AuthenticationConfiguration.fromEnvironment(Map.of(
                "TEST_USERNAME", "student@example.test")));
    }
}
