package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            assertEquals(400, ai.createConversation(Map.of("title", "x".repeat(151)), token).statusCode());
            assertEquals(400, ai.updateConversation(UNKNOWN_ID, Map.of("title", " "), token).statusCode());
            assertEquals(400, ai.chat(Map.of("conversationId", "not-a-uuid", "message", " "), token).statusCode());
            assertEquals(400, ai.generate(Map.of("prompt", " "), token).statusCode());
            assertEquals(400, ai.summarizeLesson("not-a-uuid", token).statusCode());
            assertEquals(400, ai.summarizeMaterial(UNKNOWN_ID, token).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownConversationReadUpdateAndDeleteRoutesReturnNotFound() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            assertEquals(404, ai.conversation(UNKNOWN_ID, token).statusCode());
            assertEquals(404, ai.messages(UNKNOWN_ID, token).statusCode());
            assertEquals(404, ai.updateConversation(UNKNOWN_ID, Map.of("title", "valid title"), token)
                    .statusCode());
            assertEquals(404, ai.deleteConversation(UNKNOWN_ID, token).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void publicQuizGenerationRejectsInvalidSemanticParametersBeforeProviderCall() {
        String courseId = "00000000-0000-0000-0000-000000000001";
        assertEquals(400, ai.generateQuiz(Map.of("courseId", courseId, "questionCount", "0",
                "difficulty", "EASY", "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER"), null, null).statusCode());
        assertEquals(400, ai.generateQuiz(Map.of("courseId", courseId, "questionCount", "2147483647",
                "difficulty", "EASY", "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER"), null, null).statusCode());
        assertEquals(400, ai.generateQuiz(Map.of("courseId", courseId, "questionCount", "1",
                "difficulty", "IMPOSSIBLE", "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER"), null, null).statusCode());
        assertEquals(400, ai.generateQuiz(Map.of("courseId", courseId, "questionCount", "1",
                "difficulty", "EASY", "questionType", "MULTIPLE_CHOICE"), null, null).statusCode());
    }
}
