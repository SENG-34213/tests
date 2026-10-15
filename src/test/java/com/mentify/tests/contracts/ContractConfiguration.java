package com.mentify.tests.contracts;

import java.util.Map;

public record ContractConfiguration(
        boolean enabled,
        boolean strict,
        boolean failOnExtraRequiredFields,
        boolean failOnMissingFields,
        String baseUrl,
        String courseId) {

    public static ContractConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static ContractConfiguration fromEnvironment(Map<String, String> environment) {
        return new ContractConfiguration(
                booleanValue(environment, "CONTRACT_TEST_ENABLED"),
                booleanValue(environment, "CONTRACT_SCHEMA_STRICT"),
                booleanValue(environment, "CONTRACT_FAIL_ON_EXTRA_REQUIRED_FIELDS"),
                booleanValue(environment, "CONTRACT_FAIL_ON_MISSING_FIELDS"),
                value(environment, "CONTRACT_BASE_URL", "http://localhost:8080"),
                value(environment, "CONTRACT_COURSE_ID", ""));
    }

    private static String value(Map<String, String> environment, String name, String fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static boolean booleanValue(Map<String, String> environment, String name) {
        String value = environment.get(name);
        return value != null && Boolean.parseBoolean(value.trim());
    }
}
