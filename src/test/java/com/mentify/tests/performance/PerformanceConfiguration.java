package com.mentify.tests.performance;

import java.util.Map;

public record PerformanceConfiguration(
        boolean enabled,
        String environment,
        String baseUrl,
        int warmupRequests,
        int measuredRequests,
        int concurrency,
        int timeoutMillis,
        double maxErrorRatePercent,
        long maxP95Millis,
        long maxP99Millis,
        boolean writesEnabled,
        String courseId) {

    public static PerformanceConfiguration fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static PerformanceConfiguration fromEnvironment(Map<String, String> environment) {
        return new PerformanceConfiguration(
                booleanValue(environment, "PERFORMANCE_TEST_ENABLED", false),
                value(environment, "PERFORMANCE_ENVIRONMENT", "local"),
                value(environment, "PERFORMANCE_BASE_URL", "http://localhost:8080"),
                positiveInt(environment, "PERFORMANCE_WARMUP_REQUESTS", 5),
                positiveInt(environment, "PERFORMANCE_MEASURED_REQUESTS", 30),
                positiveInt(environment, "PERFORMANCE_CONCURRENCY", 2),
                positiveInt(environment, "PERFORMANCE_TIMEOUT_MS", 10000),
                nonNegativeDouble(environment, "PERFORMANCE_MAX_ERROR_RATE_PERCENT", 1.0),
                positiveLong(environment, "PERFORMANCE_MAX_P95_MS", 2000),
                positiveLong(environment, "PERFORMANCE_MAX_P99_MS", 5000),
                booleanValue(environment, "PERFORMANCE_WRITE_ENABLED", false),
                value(environment, "PERFORMANCE_COURSE_ID", ""));
    }

    private static String value(Map<String, String> environment, String name, String fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static boolean booleanValue(Map<String, String> environment, String name, boolean fallback) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int positiveInt(Map<String, String> environment, String name, int fallback) {
        long value = positiveLong(environment, name, fallback);
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " must fit in an integer");
        }
        return (int) value;
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

    private static double nonNegativeDouble(Map<String, String> environment, String name, double fallback) {
        try {
            double value = Double.parseDouble(value(environment, name, Double.toString(fallback)));
            if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                throw new NumberFormatException("must be finite and non-negative");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid non-negative number for " + name, exception);
        }
    }
}
