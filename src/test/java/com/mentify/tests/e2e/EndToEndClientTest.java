package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EndToEndClientTest {

    @Test
    void endToEndConfigurationCanBeLoaded() {
        assertNotNull(EndToEndTestConfiguration.fromEnvironment());
    }
}
