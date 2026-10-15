package com.mentify.tests.performance;

import org.junit.jupiter.api.Test;

class ControlledConcurrencyTest extends PerformanceTestSupport {

    @Test
    void gatewayHandlesConfiguredControlledConcurrency() {
        measureConcurrent("gateway-controlled-concurrency",
                () -> api.get("/api/v1/auth/public-test"));
    }
}
