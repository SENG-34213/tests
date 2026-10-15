package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiHealthIT extends AiIntegrationTestSupport {

    @Test
    void healthEndpointReportsAiServiceUp() {
        assertEquals(200, ai.health().statusCode());
        assertEquals("ai-service", ai.health().jsonPath().getString("service"));
        assertEquals("UP", ai.health().jsonPath().getString("status"));
    }
}
