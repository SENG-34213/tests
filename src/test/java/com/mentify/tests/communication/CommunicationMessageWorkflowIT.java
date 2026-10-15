package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CommunicationMessageWorkflowIT extends CommunicationIntegrationTestSupport {

    @Test
    void teacherCanSendPageUpdateAndDeleteMessages() {
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.groupId() != null && data.writeEnabled(),
                "Communication write fixtures are not enabled");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response sent = communicationClient.sendMessage(data.groupId().toString(),
                    CommunicationTestDataFactory.message("TEST-016 " + System.nanoTime()), token);
            assertTrue(sent.statusCode() == 201 || sent.statusCode() == 200);
            String messageId = sent.jsonPath().getString("data.id");
            assertTrue(communicationClient.messageHistory(data.groupId().toString(), 0, 10, token).statusCode() == 200);
            assertTrue(communicationClient.updateMessage(data.groupId().toString(), messageId,
                    CommunicationTestDataFactory.message("updated TEST-016"), token).statusCode() == 200);
            assertTrue(communicationClient.deleteMessage(data.groupId().toString(), messageId, "EVERYONE", token)
                    .statusCode() == 200);
        } finally {
            logout(teacher);
        }
    }
}
