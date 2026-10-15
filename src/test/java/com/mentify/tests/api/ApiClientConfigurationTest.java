package com.mentify.tests.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiClientConfigurationTest {

    @Test
    void shouldUseGatewayAndTimeoutConfigurationGivenEnvironmentDefaults() {
        // Arrange
        TestEnvironmentConfiguration environment =
                TestEnvironmentConfiguration.fromEnvironment(Map.<String, String>of());

        // Act
        ApiClientConfiguration configuration =
                ApiClientConfiguration.fromEnvironment(environment, false);

        // Assert
        assertEquals(URI.create("http://localhost:8080"), configuration.baseUri());
        assertEquals(Duration.ofSeconds(5), configuration.connectTimeout());
        assertEquals(Duration.ofSeconds(10), configuration.readTimeout());
        assertFalse(configuration.loggingEnabled());
    }

    @Test
    void shouldUseConfiguredGatewayUrlGivenCustomEnvironment() {
        // Arrange
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment(Map.of(
                "API_GATEWAY_BASE_URL", "http://gateway.test:9000"));

        // Act
        ApiClientConfiguration configuration =
                ApiClientConfiguration.fromEnvironment(environment, true);

        // Assert
        assertEquals(URI.create("http://gateway.test:9000"), configuration.baseUri());
        assertEquals(RequiredService.API_GATEWAY,
                environment.serviceUrls().entrySet().stream()
                        .filter(entry -> entry.getValue().equals(configuration.baseUri()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow());
    }
}
