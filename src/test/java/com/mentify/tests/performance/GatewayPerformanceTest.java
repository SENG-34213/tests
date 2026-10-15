package com.mentify.tests.performance;

import org.junit.jupiter.api.Test;

class GatewayPerformanceTest extends PerformanceTestSupport {

    @Test
    void gatewayPublicAuthenticationProbeMeetsBaseline() {
        measure("gateway-public-authentication-probe",
                () -> api.get("/api/v1/auth/public-test"));
    }
}
