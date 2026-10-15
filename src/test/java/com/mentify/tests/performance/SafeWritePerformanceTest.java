package com.mentify.tests.performance;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SafeWritePerformanceTest extends PerformanceTestSupport {

    @Test
    void syntheticForgotPasswordValidationBaselineIsOptIn() {
        assumeTrue(performance.writesEnabled(),
                "Set PERFORMANCE_WRITE_ENABLED=true for the synthetic safe-write baseline");
        measure("synthetic-forgot-password-validation",
                () -> api.post("/api/v1/auth/forgot-password",
                        Map.of("email", "performance.invalid@example.test"), null));
    }
}
