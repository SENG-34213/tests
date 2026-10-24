package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AiAuthorizationIT extends AiIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void unauthenticatedProtectedAiRequestsReturnUnauthorized() {
        assertEquals(401, ai.me(null).statusCode());
        assertEquals(401, ai.createConversation(Map.of(), null).statusCode());
        assertEquals(401, ai.chat(Map.of(), null).statusCode());
    }

    @Test
    void studentCannotUseChatOrConversationManagement() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            var currentUser = ai.me(student.accessToken());
            assertEquals(200, currentUser.statusCode(), currentUser.asString());
            assertNotNull(UUID.fromString(currentUser.jsonPath().getString("userId")), currentUser.asString());
            assertTrue(currentUser.jsonPath().getList("roles", String.class).contains("STUDENT"),
                    currentUser.asString());
            assertEquals(403, ai.createConversation(Map.of(), student.accessToken()).statusCode());
            assertEquals(403, ai.conversations(student.accessToken()).statusCode());
            assertEquals(403, ai.chat(Map.of(), student.accessToken()).statusCode());
            assertEquals(403, ai.generate(Map.of("prompt", "synthetic"), student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void quizGenerationIsPublicAccordingToEffectiveBackendSecurity() {
        assertEquals(400, ai.generateQuiz(Map.of("courseId", "not-a-uuid", "questionCount", "1",
                "difficulty", "EASY", "questionType", "MULTIPLE_CHOICE"), null, null).statusCode());
    }
}
