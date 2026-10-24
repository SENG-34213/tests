package com.mentify.tests.security;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.user.UserClient;
import org.junit.jupiter.api.BeforeAll;

abstract class SecurityRegressionTestSupport {

    protected static ApiClient api;
    protected static AuthenticationClient authentication;
    protected static UserClient users;

    @BeforeAll
    static void requireSecurityEnvironment() {
        SecurityResilienceTestConfiguration configuration =
                SecurityResilienceTestConfiguration.fromEnvironment();
        assumeTrue(configuration.securityEnabled(),
                "Set TEST_SECURITY_ENABLED=true to run security regression tests");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitService(RequiredService.API_GATEWAY);
        readiness.awaitService(RequiredService.AUTH_SERVICE);
        readiness.awaitService(RequiredService.USER_SERVICE);
        api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        users = new UserClient(api);
    }

    protected static AuthenticationSession login(TestRole role) {
        return authentication.login(role);
    }

    protected static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authentication.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}
