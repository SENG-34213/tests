package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AuthenticationClientIT {

    private static AuthenticationClient authenticationClient;

    @BeforeAll
    static void requireBackendEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.USER_SERVICE);
        authenticationClient = new AuthenticationClient(
                new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false)),
                new ApiClient(ApiClientConfiguration.forService(environment, RequiredService.AUTH_SERVICE, false)),
                AuthenticationConfiguration.fromEnvironment());
    }

    @Test
    void shouldLoginAndCreateAnAuthenticatedApiClient() {
        AuthenticationSession session = authenticationClient.login();

        assertTrue(session.authenticated());
        assertTrue(authenticationClient.authenticatedApiClient(session) != null);
        authenticationClient.logout(session);
    }
}
