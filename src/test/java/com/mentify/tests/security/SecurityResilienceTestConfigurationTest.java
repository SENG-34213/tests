package com.mentify.tests.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SecurityResilienceTestConfigurationTest {

    @Test
    void securityAndFaultInjectionModesAreDisabledByDefault() {
        SecurityResilienceTestConfiguration configuration =
                SecurityResilienceTestConfiguration.fromEnvironment(Map.of());
        assertFalse(configuration.securityEnabled());
        assertFalse(configuration.resilienceEnabled());
        assertFalse(configuration.faultInjectionEnabled());
    }

    @Test
    void explicitModesAreReadWithoutExposingValues() {
        SecurityResilienceTestConfiguration configuration =
                SecurityResilienceTestConfiguration.fromEnvironment(Map.of(
                        "TEST_SECURITY_ENABLED", "true",
                        "TEST_RESILIENCE_ENABLED", "true",
                        "FAULT_INJECTION_ENABLED", "true",
                        "TEST_PROVIDER_FAILURE_MODE", "true",
                        "TEST_MESSAGE_BROKER_FAILURE_MODE", "true",
                        "TEST_MAIL_FAILURE_MODE", "true"));
        assertTrue(configuration.securityEnabled());
        assertTrue(configuration.resilienceEnabled());
        assertTrue(configuration.faultInjectionEnabled());
        assertTrue(configuration.providerFailureMode());
        assertTrue(configuration.messageBrokerFailureMode());
        assertTrue(configuration.mailFailureMode());
    }
}
