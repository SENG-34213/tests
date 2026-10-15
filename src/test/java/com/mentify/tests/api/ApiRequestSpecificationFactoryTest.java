package com.mentify.tests.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.environment.TestEnvironmentConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiRequestSpecificationFactoryTest {

    @Test
    void shouldCreateJsonRequestSpecificationGivenValidConfiguration() {
        ApiClientConfiguration configuration = ApiClientConfiguration.fromEnvironment(
                TestEnvironmentConfiguration.fromEnvironment(Map.<String, String>of()), false);
        ApiRequestSpecificationFactory factory = new ApiRequestSpecificationFactory(configuration);

        assertDoesNotThrow(() -> factory.create());
        assertDoesNotThrow(() -> factory.create("test-token", Map.of("X-Test-Id", "test-1")));
    }

    @Test
    void shouldRejectInvalidPathGivenPathDoesNotStartWithSlash() {
        ApiClientConfiguration configuration = ApiClientConfiguration.fromEnvironment(
                TestEnvironmentConfiguration.fromEnvironment(Map.<String, String>of()), false);
        ApiClient client = new ApiClient(configuration);

        assertThrows(IllegalArgumentException.class, () -> client.get("actuator/health"));
    }
}
