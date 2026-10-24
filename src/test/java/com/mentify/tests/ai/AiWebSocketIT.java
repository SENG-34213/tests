package com.mentify.tests.ai;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.websocket.StompWebSocketProbe;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AiWebSocketIT extends AiIntegrationTestSupport {

    @Test
    void teacherCanAuthenticateAiStompWebSocketConnection() throws Exception {
        requireAiWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            StompWebSocketProbe.assertAuthenticatedConnect(
                    StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"),
                    teacher.accessToken());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unauthenticatedAiStompConnectionsAreRejectedByServiceAndGateway() throws Exception {
        requireAiWebSocketProbe();
        StompWebSocketProbe.assertUnauthenticatedConnectRejected(
                StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"));
        StompWebSocketProbe.assertUnauthenticatedConnectRejected(
                StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws/ai"));
    }

    @Test
    void studentCannotAuthenticateToAiStompEndpoint() throws Exception {
        requireAiWebSocketProbe();
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            StompWebSocketProbe.assertConnectRejected(
                    StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"),
                    student.accessToken());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCannotUseUnapprovedAiStompDestinations() throws Exception {
        requireAiWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            StompWebSocketProbe.assertDestinationRejected(
                    StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"),
                    teacher.accessToken(), "SUBSCRIBE", "/topic/ai/private");
            StompWebSocketProbe.assertDestinationRejected(
                    StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws/ai"),
                    teacher.accessToken(), "SEND", "/app/unapproved");
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherReceivesSafeErrorOnUserQueueForUnknownConversationWithoutProviderCall() throws Exception {
        requireAiWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        UUID unknownConversation = UUID.randomUUID();
        String clientMessageId = "ws-probe-" + UUID.randomUUID();
        try {
            StompWebSocketProbe.assertAiConversationErrorDelivered(
                    StompWebSocketProbe.endpoint(RequiredService.AI_SERVICE, "/ws/ai"),
                    teacher.accessToken(), unknownConversation, clientMessageId);
            StompWebSocketProbe.assertAiConversationErrorDelivered(
                    StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws/ai"),
                    teacher.accessToken(), UUID.randomUUID(), clientMessageId + "-gateway");
        } finally {
            logout(teacher);
        }
    }

    private static void requireAiWebSocketProbe() {
        Assumptions.assumeTrue(Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_AI_WEBSOCKET_ENABLED", "false")),
                "Set TEST_AI_WEBSOCKET_ENABLED=true to enable the live AI WebSocket probes");
    }
}
