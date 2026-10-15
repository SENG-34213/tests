package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

class ProviderJourneyIT extends EndToEndIntegrationTestSupport {

    @Test
    void paymentNotificationAndLearningProviderJourneysRequireExplicitFixtures() {
        assumeTrue(EndToEndTestConfiguration.fromEnvironment().providerJourneysEnabled(),
                "Payment, notification, and learning journeys require approved deterministic provider fixtures");
        // Provider-specific journeys are intentionally gated until safe cross-service fixtures are configured.
    }
}
