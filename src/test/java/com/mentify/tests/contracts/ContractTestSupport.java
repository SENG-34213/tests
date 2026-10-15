package com.mentify.tests.contracts;

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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

abstract class ContractTestSupport {

    protected static ContractConfiguration contract;
    protected static ApiClient api;
    protected static AuthenticationClient authentication;
    protected static UserClient users;

    @BeforeAll
    static void requireContractEnvironment() {
        contract = ContractConfiguration.fromEnvironment();
        assumeTrue(contract.enabled(), "Set CONTRACT_TEST_ENABLED=true to run contract tests");
        Map<String, String> environmentValues = new HashMap<>(System.getenv());
        environmentValues.put("API_GATEWAY_BASE_URL", contract.baseUrl());
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment(environmentValues);
        BackendReadinessClient readiness = new BackendReadinessClient(environment);
        readiness.awaitService(RequiredService.API_GATEWAY);
        api = new ApiClient(new ApiClientConfiguration(
                URI.create(contract.baseUrl()),
                environment.connectTimeout(),
                environment.readTimeout(),
                false));
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
