package com.mentify.tests.environment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackendReadinessIT {

    private static BackendReadinessClient readinessClient;

    @BeforeAll
    static void verifyBackendEnvironment() {
        TestEnvironmentConfiguration configuration = TestEnvironmentConfiguration.fromEnvironment();
        readinessClient = new BackendReadinessClient(configuration);
        readinessClient.awaitRequiredServices();
    }

    @Test
    void shouldKeepRequiredBackendServicesReady() {
        readinessClient.awaitRequiredServices();
    }
}
