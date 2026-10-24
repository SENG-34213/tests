package com.mentify.tests.communication;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.websocket.StompWebSocketProbe;
import com.mentify.tests.environment.RequiredService;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CommunicationWebSocketIT extends CommunicationIntegrationTestSupport {

    @Test
    void teacherCanAuthenticateStompWebSocketConnection() throws Exception {
        requireWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            StompWebSocketProbe.assertAuthenticatedConnect(
                    StompWebSocketProbe.endpoint(RequiredService.COMMUNICATION_SERVICE, "/ws"),
                    teacher.accessToken());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unauthenticatedStompConnectIsRejectedByServiceAndGateway() throws Exception {
        requireWebSocketProbe();
        StompWebSocketProbe.assertUnauthenticatedConnectRejected(
                StompWebSocketProbe.endpoint(RequiredService.COMMUNICATION_SERVICE, "/ws"));
        StompWebSocketProbe.assertUnauthenticatedConnectRejected(
                StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws"));
    }

    @Test
    void teacherCannotSendOrSubscribeToGroupWithoutMembership() throws Exception {
        requireWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        UUID nonMemberGroup = UUID.randomUUID();
        try {
            StompWebSocketProbe.assertGroupSubscriptionRejected(
                    StompWebSocketProbe.endpoint(RequiredService.COMMUNICATION_SERVICE, "/ws"),
                    teacher.accessToken(), nonMemberGroup);
            StompWebSocketProbe.assertGroupSendRejected(
                    StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws"),
                    teacher.accessToken(), nonMemberGroup);
        } finally {
            logout(teacher);
        }
    }

    @Test
    void authenticatedTeacherCanSendAndReceiveTestBroadcastDirectlyAndThroughGateway() throws Exception {
        requireWebSocketProbe();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String directContent = "direct-" + UUID.randomUUID();
            StompWebSocketProbe.assertTestTopicRoundTrip(
                    StompWebSocketProbe.endpoint(RequiredService.COMMUNICATION_SERVICE, "/ws"),
                    teacher.accessToken(), directContent);
            String gatewayContent = "gateway-" + UUID.randomUUID();
            StompWebSocketProbe.assertTestTopicRoundTrip(
                    StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws"),
                    teacher.accessToken(), gatewayContent);
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherCanSendPersistedGroupMessageOverGatewayAndCleanItUp() throws Exception {
        requireWebSocketProbe();
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.groupId() != null && data.writeEnabled(),
                "Set TEST_COMMUNICATION_GROUP_ID and TEST_COMMUNICATION_WRITE_ENABLED=true; "
                        + "the teacher must be an active member of the group");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String groupId = data.groupId().toString();
        String messageId = null;
        try {
            String content = "STOMP integration " + UUID.randomUUID();
            messageId = StompWebSocketProbe.assertGroupMessageRoundTrip(
                    StompWebSocketProbe.endpoint(RequiredService.API_GATEWAY, "/ws"),
                    teacher.accessToken(), data.groupId(), content);
            Response removed = communicationClient.deleteMessage(groupId, messageId,
                    "EVERYONE", teacher.accessToken());
            Assertions.assertEquals(200, removed.statusCode(), removed.asString());
            messageId = null;
        } finally {
            if (messageId != null) {
                communicationClient.deleteMessage(groupId, messageId, "EVERYONE", teacher.accessToken());
            }
            logout(teacher);
        }
    }

    private static void requireWebSocketProbe() {
        Assumptions.assumeTrue(Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_COMMUNICATION_WEBSOCKET_ENABLED", "false")),
                "Set TEST_COMMUNICATION_WEBSOCKET_ENABLED=true to enable the live WebSocket probes");
    }

}
