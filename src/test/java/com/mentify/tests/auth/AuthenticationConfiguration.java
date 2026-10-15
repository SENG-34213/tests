package com.mentify.tests.auth;

import java.util.EnumMap;
import java.util.Map;

public record AuthenticationConfiguration(
        TestCredentials defaultCredentials,
        Map<TestRole, TestCredentials> roleCredentials,
        boolean loggingEnabled) {

    public static AuthenticationConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    public static AuthenticationConfiguration fromEnvironment(Map<String, String> environment) {
        EnumMap<TestRole, TestCredentials> credentials = new EnumMap<>(TestRole.class);
        for (TestRole role : TestRole.values()) {
            TestCredentials roleCredentials = credentials(environment,
                    role.name() + "_TEST_USERNAME", role.name() + "_TEST_PASSWORD");
            if (roleCredentials != null) {
                credentials.put(role, roleCredentials);
            }
        }
        return new AuthenticationConfiguration(
                credentials(environment, "TEST_USERNAME", "TEST_PASSWORD"),
                Map.copyOf(credentials),
                booleanValue(environment, "TEST_AUTH_LOGGING_ENABLED", false));
    }

    public TestCredentials credentialsFor(TestRole role) {
        TestCredentials credentials = role == null ? null : roleCredentials.get(role);
        return credentials == null ? requireDefaultCredentials() : credentials;
    }

    public TestCredentials requireDefaultCredentials() {
        if (defaultCredentials == null) {
            throw new IllegalStateException(
                    "Authentication credentials are not configured. Set TEST_USERNAME and TEST_PASSWORD.");
        }
        return defaultCredentials;
    }

    private static TestCredentials credentials(Map<String, String> environment,
                                               String usernameName, String passwordName) {
        String username = value(environment, usernameName);
        String password = value(environment, passwordName);
        if (username == null && password == null) {
            return null;
        }
        if (username == null || password == null) {
            throw new IllegalArgumentException(
                    "Both " + usernameName + " and " + passwordName + " must be configured together");
        }
        return new TestCredentials(username, password);
    }

    private static String value(Map<String, String> environment, String name) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static boolean booleanValue(Map<String, String> environment, String name, boolean fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }
}
