package com.mentify.tests.e2e;

import java.util.Map;

record EndToEndTestConfiguration(boolean enabled, String courseId, boolean providerJourneysEnabled) {

    static EndToEndTestConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new EndToEndTestConfiguration(
                Boolean.parseBoolean(environment.getOrDefault("TEST_E2E_ENABLED", "false")),
                environment.get("TEST_E2E_COURSE_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_E2E_PROVIDER_JOURNEYS_ENABLED", "false")));
    }
}
