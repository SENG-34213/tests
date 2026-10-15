package com.mentify.tests.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PerformanceConfigurationTest {

    @Test
    void performanceIsDisabledAndSafeByDefault() {
        PerformanceConfiguration configuration = PerformanceConfiguration.fromEnvironment(Map.of());
        assertFalse(configuration.enabled());
        assertEquals(5, configuration.warmupRequests());
        assertEquals(30, configuration.measuredRequests());
        assertEquals(2, configuration.concurrency());
        assertFalse(configuration.writesEnabled());
    }

    @Test
    void configuredThresholdsAndBoundsAreRead() {
        PerformanceConfiguration configuration = PerformanceConfiguration.fromEnvironment(Map.ofEntries(
                Map.entry("PERFORMANCE_TEST_ENABLED", "true"),
                Map.entry("PERFORMANCE_ENVIRONMENT", "dedicated-local"),
                Map.entry("PERFORMANCE_WARMUP_REQUESTS", "1"),
                Map.entry("PERFORMANCE_MEASURED_REQUESTS", "4"),
                Map.entry("PERFORMANCE_CONCURRENCY", "2"),
                Map.entry("PERFORMANCE_TIMEOUT_MS", "500"),
                Map.entry("PERFORMANCE_MAX_ERROR_RATE_PERCENT", "2.5"),
                Map.entry("PERFORMANCE_MAX_P95_MS", "1000"),
                Map.entry("PERFORMANCE_MAX_P99_MS", "2000"),
                Map.entry("PERFORMANCE_WRITE_ENABLED", "true"),
                Map.entry("PERFORMANCE_COURSE_ID", "course-fixture")));
        assertEquals("dedicated-local", configuration.environment());
        assertEquals("http://localhost:8080", configuration.baseUrl());
        assertEquals(500, configuration.timeoutMillis());
        assertEquals(2.5, configuration.maxErrorRatePercent());
        assertEquals("course-fixture", configuration.courseId());
    }

    @Test
    void invalidThresholdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PerformanceConfiguration.fromEnvironment(
                Map.of("PERFORMANCE_MAX_P95_MS", "0")));
        assertThrows(IllegalArgumentException.class, () -> PerformanceConfiguration.fromEnvironment(
                Map.of("PERFORMANCE_MAX_ERROR_RATE_PERCENT", "-1")));
    }
}
