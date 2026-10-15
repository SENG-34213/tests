package com.mentify.tests.resilience;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.security.SecurityResilienceTestConfiguration;

abstract class ResilienceTestSupport {

    protected static SecurityResilienceTestConfiguration requireFaultInjection() {
        SecurityResilienceTestConfiguration configuration =
                SecurityResilienceTestConfiguration.fromEnvironment();
        assumeTrue(configuration.resilienceEnabled(),
                "Set TEST_RESILIENCE_ENABLED=true to run resilience regression tests");
        assumeTrue(configuration.faultInjectionEnabled(),
                "Set FAULT_INJECTION_ENABLED=true with controlled fault infrastructure");
        return configuration;
    }
}
