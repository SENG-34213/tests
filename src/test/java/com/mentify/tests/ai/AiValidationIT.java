package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiValidationIT extends AiIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void invalidAiRequestsReturnClientErrorsOrNotFoundResponses() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            assertTrue(ai.createConversation(Map.of("title", "x".repeat(151)), token).statusCode() < 500);
            assertTrue(ai.updateConversation(UNKNOWN_ID, Map.of("title", " "), token).statusCode() < 500);
            assertTrue(ai.chat(Map.of("conversationId", "not-a-uuid", "message", " "), token).statusCode() < 500);
            assertTrue(ai.generate(Map.of("prompt", " "), token).statusCode() < 500);
            assertTrue(ai.summarizeLesson("not-a-uuid", token).statusCode() < 500);
            assertTrue(ai.summarizeMaterial(UNKNOWN_ID, token).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }
}
