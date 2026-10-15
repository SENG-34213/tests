package com.mentify.tests.environment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BackendReadinessClientTest {

    @Test
    void shouldCompleteGivenRequiredServiceReturnsSuccessfulStatus() {
        TestEnvironmentConfiguration configuration = configuration(true, Duration.ofSeconds(1));
        BackendReadinessClient client = new BackendReadinessClient(
                configuration, endpoint -> 200, millis -> { });

        assertDoesNotThrow(client::awaitRequiredServices);
    }

    @Test
    void shouldRetryGivenServiceTemporarilyFails() {
        TestEnvironmentConfiguration configuration = configuration(true, Duration.ofSeconds(1));
        int[] attempts = {0};
        BackendReadinessClient client = new BackendReadinessClient(
                configuration,
                endpoint -> ++attempts[0] == 1 ? 503 : 200,
                millis -> { });

        assertDoesNotThrow(client::awaitRequiredServices);
    }

    @Test
    void shouldFailClearlyGivenEnvironmentIsDisabled() {
        TestEnvironmentConfiguration configuration = configuration(false, Duration.ofSeconds(1));
        BackendReadinessClient client = new BackendReadinessClient(
                configuration, endpoint -> 200, millis -> { });

        assertThrows(BackendReadinessException.class, client::awaitRequiredServices);
    }

    @Test
    void shouldFailAfterTimeoutGivenServiceNeverBecomesReady() {
        TestEnvironmentConfiguration configuration = configuration(true, Duration.ofMillis(10));
        BackendReadinessClient client = new BackendReadinessClient(
                configuration, endpoint -> 503, millis -> { });

        assertThrows(BackendReadinessException.class, client::awaitRequiredServices);
    }

    private static TestEnvironmentConfiguration configuration(boolean enabled, Duration timeout) {
        Map<RequiredService, java.net.URI> urls = new EnumMap<>(RequiredService.class);
        urls.put(RequiredService.API_GATEWAY, java.net.URI.create("http://localhost:8080"));
        urls.put(RequiredService.KEYCLOAK, java.net.URI.create("http://localhost:8180"));
        return new TestEnvironmentConfiguration(
                enabled,
                List.of(RequiredService.API_GATEWAY),
                urls,
                "mentify",
                timeout,
                Duration.ZERO,
                Duration.ofMillis(100),
                Duration.ofMillis(100));
    }
}
