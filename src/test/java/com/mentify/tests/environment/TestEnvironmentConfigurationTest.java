package com.mentify.tests.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestEnvironmentConfigurationTest {

    @Test
    void shouldUseSafeDefaultsGivenEnvironmentIsNotConfigured() {
        // Arrange and Act
        TestEnvironmentConfiguration configuration = TestEnvironmentConfiguration.fromEnvironment(Map.of());

        // Assert
        assertFalse(configuration.enabled());
        assertEquals(URI.create("http://localhost:8080"),
                configuration.serviceUrls().get(RequiredService.API_GATEWAY));
        assertEquals(URI.create("http://localhost:8180"),
                configuration.serviceUrls().get(RequiredService.KEYCLOAK));
    }

    @Test
    void shouldParseConfiguredServiceNamesGivenValidDefaults() {
        // Arrange and Act
        TestEnvironmentConfiguration configuration = TestEnvironmentConfiguration.fromEnvironment(Map.of());

        // Assert
        assertEquals(
                java.util.List.of(RequiredService.API_GATEWAY, RequiredService.KEYCLOAK),
                configuration.requiredServices());
    }

    @Test
    void shouldUseLegacyBackendBaseUrlAsGatewayFallbackGivenGatewayUrlIsAbsent() {
        // Arrange
        java.util.Map<String, String> environment = java.util.Map.of(
                "BACKEND_BASE_URL", "http://test-backend:8080");

        // Act
        TestEnvironmentConfiguration configuration =
                TestEnvironmentConfiguration.fromEnvironment(environment);

        // Assert
        assertEquals(URI.create("http://test-backend:8080"),
                configuration.serviceUrls().get(RequiredService.API_GATEWAY));
    }

    @Test
    void shouldParseCalendarServiceConfigurationWhenCalendarTestingIsEnabled() {
        Map<String, String> environment = Map.of(
                "TEST_REQUIRED_SERVICES", "API_GATEWAY,KEYCLOAK,CALENDAR_SERVICE",
                "CALENDAR_SERVICE_BASE_URL", "http://calendar.test:8092");

        TestEnvironmentConfiguration configuration =
                TestEnvironmentConfiguration.fromEnvironment(environment);

        assertEquals(java.util.List.of(
                        RequiredService.API_GATEWAY,
                        RequiredService.KEYCLOAK,
                        RequiredService.CALENDAR_SERVICE),
                configuration.requiredServices());
        assertEquals(URI.create("http://calendar.test:8092"),
                configuration.serviceUrls().get(RequiredService.CALENDAR_SERVICE));
    }

    @Test
    void shouldParseCourseServiceConfigurationWhenCourseTestingIsEnabled() {
        Map<String, String> environment = Map.of(
                "TEST_REQUIRED_SERVICES", "API_GATEWAY,KEYCLOAK,COURSE_SERVICE",
                "COURSE_SERVICE_BASE_URL", "http://course.test:8082");

        TestEnvironmentConfiguration configuration =
                TestEnvironmentConfiguration.fromEnvironment(environment);

        assertEquals(java.util.List.of(
                        RequiredService.API_GATEWAY,
                        RequiredService.KEYCLOAK,
                        RequiredService.COURSE_SERVICE),
                configuration.requiredServices());
        assertEquals(URI.create("http://course.test:8082"),
                configuration.serviceUrls().get(RequiredService.COURSE_SERVICE));
    }

    @Test
    void shouldParseEnrollmentServiceConfigurationWhenEnrollmentTestingIsEnabled() {
        Map<String, String> environment = Map.of(
                "TEST_REQUIRED_SERVICES", "API_GATEWAY,KEYCLOAK,COURSE_SERVICE,ENROLLMENT_SERVICE",
                "ENROLLMENT_SERVICE_BASE_URL", "http://enrollment.test:8083");

        TestEnvironmentConfiguration configuration =
                TestEnvironmentConfiguration.fromEnvironment(environment);

        assertEquals(java.util.List.of(
                        RequiredService.API_GATEWAY,
                        RequiredService.KEYCLOAK,
                        RequiredService.COURSE_SERVICE,
                        RequiredService.ENROLLMENT_SERVICE),
                configuration.requiredServices());
        assertEquals(URI.create("http://enrollment.test:8083"),
                configuration.serviceUrls().get(RequiredService.ENROLLMENT_SERVICE));
    }
}
