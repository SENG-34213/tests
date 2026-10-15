package com.mentify.tests.resilience;

import org.junit.jupiter.api.Test;

class DownstreamFailureIT extends ResilienceTestSupport {

    @Test
    void controlledDownstreamFailureRequiresApprovedFaultInjection() {
        requireFaultInjection();
        // The concrete failure mechanism belongs to the controlled test environment.
        // This test must remain skipped when no fault injector is configured.
    }
}
