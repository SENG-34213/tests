package com.mentify.tests.resilience;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.security.SecurityResilienceTestConfiguration;
import org.junit.jupiter.api.Test;

class ProviderFailureIT extends ResilienceTestSupport {

    @Test
    void providerFailureRequiresExplicitProviderFailureMode() {
        SecurityResilienceTestConfiguration configuration = requireFaultInjection();
        assumeTrue(configuration.providerFailureMode(),
                "Set TEST_PROVIDER_FAILURE_MODE=true with a controlled provider double");
    }
}
