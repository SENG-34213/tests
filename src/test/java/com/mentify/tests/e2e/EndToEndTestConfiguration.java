package com.mentify.tests.e2e;

import java.util.Map;

record EndToEndTestConfiguration(boolean enabled, String courseId, boolean providerJourneysEnabled,
                                 boolean writeEnabled, String courseGradeId) {

    static EndToEndTestConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static EndToEndTestConfiguration fromEnvironment(Map<String, String> environment) {
        return new EndToEndTestConfiguration(
                Boolean.parseBoolean(environment.getOrDefault("TEST_E2E_ENABLED", "false")),
                environment.get("TEST_E2E_COURSE_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_E2E_PROVIDER_JOURNEYS_ENABLED", "false")),
                Boolean.parseBoolean(environment.getOrDefault("TEST_E2E_WRITE_ENABLED", "false")),
                environment.get("TEST_E2E_COURSE_GRADE_ID"));
    }
}
