package com.mentify.tests.auth;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

abstract class AuthenticationIntegrationTestSupport {

    protected static ApiClient apiClient;
    protected static AuthenticationClient authenticationClient;
    protected static AuthenticationConfiguration authenticationConfiguration;

    @BeforeAll
    static void requireBackendEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitRequiredServices();
        apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authenticationConfiguration = AuthenticationConfiguration.fromEnvironment();
        authenticationClient = new AuthenticationClient(apiClient, authenticationConfiguration);
    }

    @AfterEach
    void clearAuthenticationState() {
        // Each test owns and clears its session; this hook documents the isolation requirement.
    }

    protected static AuthenticationSession login(TestRole role) {
        return authenticationClient.login(role);
    }

    protected static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authenticationClient.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }

    protected static Response post(String path, Object body) {
        return apiClient.post(path, body, null);
    }
}
