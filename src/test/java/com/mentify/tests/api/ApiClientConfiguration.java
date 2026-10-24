package com.mentify.tests.api;

import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.environment.RequiredService;
import java.net.URI;
import java.time.Duration;

public record ApiClientConfiguration(
        URI baseUri,
        Duration connectTimeout,
        Duration readTimeout,
        boolean loggingEnabled) {

    public static ApiClientConfiguration fromEnvironment() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        return new ApiClientConfiguration(
                environment.serviceUrls().get(com.mentify.tests.environment.RequiredService.API_GATEWAY),
                environment.connectTimeout(),
                environment.readTimeout(),
                booleanValue("API_LOGGING_ENABLED", false));
    }

    public static ApiClientConfiguration fromEnvironment(TestEnvironmentConfiguration environment,
                                                         boolean loggingEnabled) {
        return forService(environment, RequiredService.API_GATEWAY, loggingEnabled);
    }

    public static ApiClientConfiguration forService(TestEnvironmentConfiguration environment,
                                                    RequiredService service, boolean loggingEnabled) {
        return new ApiClientConfiguration(
                environment.serviceUrls().get(service),
                environment.connectTimeout(),
                environment.readTimeout(),
                loggingEnabled);
    }

    private static boolean booleanValue(String name, boolean fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }
}
