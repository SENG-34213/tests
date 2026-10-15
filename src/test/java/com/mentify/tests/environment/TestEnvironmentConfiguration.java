package com.mentify.tests.environment;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record TestEnvironmentConfiguration(
        boolean enabled,
        List<RequiredService> requiredServices,
        Map<RequiredService, URI> serviceUrls,
        String keycloakRealm,
        Duration readinessTimeout,
        Duration pollInterval,
        Duration connectTimeout,
        Duration readTimeout) {

    public static TestEnvironmentConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static TestEnvironmentConfiguration fromEnvironment(Map<String, String> environment) {
        Map<RequiredService, URI> urls = new EnumMap<>(RequiredService.class);
        for (RequiredService service : RequiredService.values()) {
            String fallback = service == RequiredService.API_GATEWAY
                    ? value(environment, "BACKEND_BASE_URL", defaultUrl(service))
                    : defaultUrl(service);
            urls.put(service, uri(environment, service, environmentVariable(service), fallback));
        }

        return new TestEnvironmentConfiguration(
                booleanValue(environment, "TEST_ENV_ENABLED", false),
                configuredRequiredServices(environment),
                urls,
                value(environment, "KEYCLOAK_REALM", "mentify"),
                Duration.ofSeconds(positiveLong(environment, "READINESS_TIMEOUT_SECONDS", 120)),
                Duration.ofMillis(positiveLong(environment, "READINESS_POLL_INTERVAL_MILLIS", 2000)),
                Duration.ofMillis(positiveLong(environment, "HTTP_CONNECT_TIMEOUT_MILLIS", 5000)),
                Duration.ofMillis(positiveLong(environment, "HTTP_READ_TIMEOUT_MILLIS", 10000)));
    }

    private static List<RequiredService> configuredRequiredServices(Map<String, String> environment) {
        String configured = value(environment, "TEST_REQUIRED_SERVICES", "API_GATEWAY,KEYCLOAK");
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(RequiredService::parse)
                .toList();
    }

    private static URI uri(
            Map<String, String> environment, RequiredService service, String variable, String fallback) {
        String raw = value(environment, variable, fallback);
        try {
            URI uri = URI.create(raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw);
            if (uri.getScheme() == null || uri.getHost() == null
                    || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
                throw new IllegalArgumentException("must be an http or https URL");
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + variable + " for " + service + ": " + raw, exception);
        }
    }

    private static String environmentVariable(RequiredService service) {
        return switch (service) {
            case API_GATEWAY -> "API_GATEWAY_BASE_URL";
            case KEYCLOAK -> "KEYCLOAK_BASE_URL";
            case CONFIG_SERVER -> "CONFIG_SERVER_BASE_URL";
            case EUREKA -> "EUREKA_BASE_URL";
            case USER_SERVICE -> "USER_SERVICE_BASE_URL";
            case COURSE_SERVICE -> "COURSE_SERVICE_BASE_URL";
            case ENROLLMENT_SERVICE -> "ENROLLMENT_SERVICE_BASE_URL";
            case ASSIGNMENT_SERVICE -> "ASSIGNMENT_SERVICE_BASE_URL";
            case COMMUNICATION_SERVICE -> "COMMUNICATION_SERVICE_BASE_URL";
            case ATTENDANCE_SERVICE -> "ATTENDANCE_SERVICE_BASE_URL";
            case AI_SERVICE -> "AI_SERVICE_BASE_URL";
            case EXAM_SERVICE -> "EXAM_SERVICE_BASE_URL";
            case PAYMENT_SERVICE -> "PAYMENT_SERVICE_BASE_URL";
            case MATERIAL_SERVICE -> "MATERIAL_SERVICE_BASE_URL";
            case CALENDAR_SERVICE -> "CALENDAR_SERVICE_BASE_URL";
            case NOTIFICATION_SERVICE -> "NOTIFICATION_SERVICE_BASE_URL";
        };
    }

    private static String defaultUrl(RequiredService service) {
        return switch (service) {
            case API_GATEWAY -> "http://localhost:8080";
            case KEYCLOAK -> "http://localhost:8180";
            case CONFIG_SERVER -> "http://localhost:8888";
            case EUREKA -> "http://localhost:8761";
            case USER_SERVICE -> "http://localhost:8081";
            case COURSE_SERVICE -> "http://localhost:8082";
            case ENROLLMENT_SERVICE -> "http://localhost:8083";
            case ASSIGNMENT_SERVICE -> "http://localhost:8084";
            case COMMUNICATION_SERVICE -> "http://localhost:8086";
            case ATTENDANCE_SERVICE -> "http://localhost:8087";
            case AI_SERVICE -> "http://localhost:8088";
            case EXAM_SERVICE -> "http://localhost:8089";
            case PAYMENT_SERVICE -> "http://localhost:8090";
            case MATERIAL_SERVICE -> "http://localhost:8091";
            case CALENDAR_SERVICE -> "http://localhost:8092";
            case NOTIFICATION_SERVICE -> "http://localhost:8093";
        };
    }

    private static String value(Map<String, String> environment, String name, String fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static boolean booleanValue(Map<String, String> environment, String name, boolean fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }

    private static long positiveLong(Map<String, String> environment, String name, long fallback) {
        try {
            long value = Long.parseLong(value(environment, name, Long.toString(fallback)));
            if (value <= 0) {
                throw new NumberFormatException("must be positive");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid positive number for " + name, exception);
        }
    }
}
