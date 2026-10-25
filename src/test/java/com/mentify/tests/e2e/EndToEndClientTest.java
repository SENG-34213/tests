package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EndToEndClientTest {

    @Test
    void endToEndConfigurationCanBeLoaded() {
        assertNotNull(EndToEndTestConfiguration.fromEnvironment());
    }

    @Test
    void writeJourneysAreDisabledUnlessExplicitlyEnabled() {
        EndToEndTestConfiguration configuration = EndToEndTestConfiguration.fromEnvironment(Map.of());

        assertFalse(configuration.enabled());
        assertFalse(configuration.writeEnabled());
        assertFalse(configuration.providerJourneysEnabled());
        assertNull(configuration.courseGradeId());
    }

    @Test
    void loadsExplicitWriteJourneyConfiguration() {
        EndToEndTestConfiguration configuration = EndToEndTestConfiguration.fromEnvironment(Map.of(
                "TEST_E2E_ENABLED", "true",
                "TEST_E2E_WRITE_ENABLED", "true",
                "TEST_E2E_COURSE_GRADE_ID", "grade-fixture"));

        assertTrue(configuration.enabled());
        assertTrue(configuration.writeEnabled());
        assertEquals("grade-fixture", configuration.courseGradeId());
    }
}
