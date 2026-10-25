package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.security.SecurityResilienceTestConfiguration;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiProviderFailureIT extends AiIntegrationTestSupport {

    @Test
    void controlledProviderFailureReturnsSanitizedServerError() {
        SecurityResilienceTestConfiguration configuration =
                SecurityResilienceTestConfiguration.fromEnvironment();
        assumeTrue(configuration.resilienceEnabled(), "Set TEST_RESILIENCE_ENABLED=true");
        assumeTrue(configuration.faultInjectionEnabled(), "Set FAULT_INJECTION_ENABLED=true");
        assumeTrue(configuration.providerFailureMode(),
                "Set TEST_PROVIDER_FAILURE_MODE=true and configure the AI Service to use the controlled failing provider");

        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = ai.generate(
                    Map.of("prompt", "synthetic controlled provider failure probe"), admin.accessToken());
            String body = response.asString();
            assertTrue(response.statusCode() >= 500 && response.statusCode() < 600,
                    "Expected a server error from the controlled provider failure, got "
                            + response.statusCode() + ": " + body);
            assertFalse(body.contains("CONTROLLED_PROVIDER_ERROR_SENTINEL"),
                    "Provider response details must not leak to the API client");
        } finally {
            logout(admin);
        }
    }
}
