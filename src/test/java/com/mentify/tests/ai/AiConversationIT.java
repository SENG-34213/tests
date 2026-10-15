package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiConversationIT extends AiIntegrationTestSupport {

    @Test
    void teacherCanUseTheConversationLifecycle() {
        AiTestData data = AiTestData.fromEnvironment();
        assumeTrue(data.writeEnabled(), "Set TEST_AI_WRITE_ENABLED=true for conversation workflow tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response created = ai.createConversation(Map.of("title", "TEST-020 " + System.nanoTime()), token);
            assertEquals(201, created.statusCode());
            String conversationId = created.jsonPath().getString("data.id");
            assertTrue(conversationId != null && !conversationId.isBlank());
            assertEquals(200, ai.conversations(token).statusCode());
            assertEquals(200, ai.conversation(conversationId, token).statusCode());
            assertEquals(200, ai.messages(conversationId, token).statusCode());
            assertEquals(200, ai.updateConversation(conversationId, Map.of("title", "Updated TEST-020"), token)
                    .statusCode());
            assertEquals(200, ai.deleteConversation(conversationId, token).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
