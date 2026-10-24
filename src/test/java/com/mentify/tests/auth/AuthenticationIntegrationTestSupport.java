package com.mentify.tests.auth;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
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
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.USER_SERVICE);
        apiClient = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authenticationConfiguration = AuthenticationConfiguration.fromEnvironment();
        ApiClient authServiceClient = new ApiClient(ApiClientConfiguration.forService(
                environment, RequiredService.AUTH_SERVICE, false));
        authenticationClient = new AuthenticationClient(apiClient, authServiceClient, authenticationConfiguration);
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
