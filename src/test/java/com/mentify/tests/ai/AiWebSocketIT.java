package com.mentify.tests.ai;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.websocket.StompWebSocketProbe;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AiWebSocketIT extends AiIntegrationTestSupport {

    @Test
    void teacherCanAuthenticateAiStompWebSocketConnection() throws Exception {
        Assumptions.assumeTrue(Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_AI_WEBSOCKET_ENABLED", "false")),
                "Set TEST_AI_WEBSOCKET_ENABLED=true to enable the live AI WebSocket probe");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            StompWebSocketProbe.assertAuthenticatedConnect(
                    StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"),
                    teacher.accessToken());
        } finally {
            logout(teacher);
        }
    }
}
