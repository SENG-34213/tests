package com.mentify.tests.api;

import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ApiClientIT {

    private static ApiClient client;

    @BeforeAll
    static void verifyBackendAndCreateClient() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitRequiredServices();
        client = new ApiClient(ApiClientConfiguration.fromEnvironment());
    }

    @Test
    void shouldReadGatewayHealthAfterReadinessPasses() {
        ApiResponseAssertions.assertStatus(client.get("/actuator/health"), 200);
    }
}
