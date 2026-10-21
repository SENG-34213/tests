package com.mentify.tests.communication;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.websocket.StompWebSocketProbe;
import com.mentify.tests.environment.RequiredService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CommunicationWebSocketIT extends CommunicationIntegrationTestSupport {

    @Test
    void teacherCanAuthenticateStompWebSocketConnection() throws Exception {
        Assumptions.assumeTrue(Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_COMMUNICATION_WEBSOCKET_ENABLED", "false")),
                "Set TEST_COMMUNICATION_WEBSOCKET_ENABLED=true to enable the live WebSocket probe");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            StompWebSocketProbe.assertAuthenticatedConnect(
                    StompWebSocketProbe.endpoint(RequiredService.COMMUNICATION_SERVICE, "/ws"),
                    teacher.accessToken());
        } finally {
            logout(teacher);
        }
    }

}
