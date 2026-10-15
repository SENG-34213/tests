package com.mentify.tests.user;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import com.mentify.tests.auth.AuthenticationClient;
import com.mentify.tests.auth.AuthenticationConfiguration;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import org.junit.jupiter.api.BeforeAll;

abstract class UserIntegrationTestSupport {

    protected static UserClient users;
    protected static AuthenticationClient authentication;

    @BeforeAll
    static void requireUserEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitRequiredServices();
        readiness.awaitService(RequiredService.USER_SERVICE);
        ApiClient api = new ApiClient(ApiClientConfiguration.fromEnvironment(environment, false));
        authentication = new AuthenticationClient(api, AuthenticationConfiguration.fromEnvironment());
        users = new UserClient(api);
    }

    protected static AuthenticationSession login(TestRole role) { return authentication.login(role); }

    protected static void logout(AuthenticationSession session) {
        if (session != null && session.refreshable()) {
            authentication.logout(session);
        } else if (session != null) {
            session.clear();
        }
    }
}
