package com.mentify.tests.security;

import java.util.Map;

public record SecurityResilienceTestConfiguration(
        boolean securityEnabled,
        boolean resilienceEnabled,
        boolean faultInjectionEnabled,
        boolean providerFailureMode,
        boolean messageBrokerFailureMode,
        boolean mailFailureMode) {

    public static SecurityResilienceTestConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static SecurityResilienceTestConfiguration fromEnvironment(Map<String, String> environment) {
        return new SecurityResilienceTestConfiguration(
                booleanValue(environment, "TEST_SECURITY_ENABLED"),
                booleanValue(environment, "TEST_RESILIENCE_ENABLED"),
                booleanValue(environment, "FAULT_INJECTION_ENABLED"),
                booleanValue(environment, "TEST_PROVIDER_FAILURE_MODE"),
                booleanValue(environment, "TEST_MESSAGE_BROKER_FAILURE_MODE"),
                booleanValue(environment, "TEST_MAIL_FAILURE_MODE"));
    }

    private static boolean booleanValue(Map<String, String> environment, String name) {
        String value = environment.get(name);
        return value != null && Boolean.parseBoolean(value.trim());
    }
}
