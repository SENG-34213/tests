package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiContentGenerationIT extends AiIntegrationTestSupport {

    @Test
    void shouldSummarizeConfiguredLessonWithDeterministicProvider() {
        AiTestData data = requireAiFixture("lesson");
        requireService(RequiredService.COURSE_SERVICE);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = ai.summarizeLesson(data.lessonId().toString(), teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(data.lessonId().toString(), response.jsonPath().getString("data.lessonId"));
            assertFalse(response.jsonPath().getString("data.summary").isBlank(), response.asString());
            assertNotNull(response.jsonPath().get("data.provider"), response.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldSummarizeConfiguredMaterialWithDeterministicProvider() {
        AiTestData data = requireAiFixture("material");
        requireService(RequiredService.MATERIAL_SERVICE);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = ai.summarizeMaterial(data.materialId().toString(), teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertFalse(response.jsonPath().getString("data.summary").isBlank(), response.asString());
            assertNotNull(response.jsonPath().get("data.provider"), response.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void shouldGenerateQuizDraftWithDeterministicProvider() {
        AiTestData data = requireAiFixture("course");
        requireService(RequiredService.COURSE_SERVICE);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = ai.generateQuiz(Map.of(
                    "courseId", data.courseId().toString(),
                    "questionCount", "3",
                    "difficulty", "EASY",
                    "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER",
                    "userPrompt", "Create a short synthetic quiz about Java basics."), null,
                    teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertNotNull(response.jsonPath().get("data"), response.asString());
            assertNotNull(response.jsonPath().get("data.questions"), response.asString());
            assertEquals(3, response.jsonPath().getList("data.questions").size(), response.asString());
            assertEquals("MULTIPLE_CHOICE_SINGLE_ANSWER",
                    response.jsonPath().getString("data.questionType"), response.asString());
        } finally {
            logout(teacher);
        }
    }

    private static AiTestData requireAiFixture(String requiredFixture) {
        AiTestData data = AiTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.providerEnabled(),
                "Enable TEST_AI_WRITE_ENABLED=true and AI_TEST_MODE=true with a deterministic provider");
        switch (requiredFixture) {
            case "lesson" -> assumeTrue(data.lessonId() != null, "Set TEST_AI_LESSON_ID");
            case "material" -> assumeTrue(data.materialId() != null, "Set TEST_AI_MATERIAL_ID");
            case "course" -> assumeTrue(data.courseId() != null, "Set TEST_AI_COURSE_ID");
            default -> throw new IllegalArgumentException("Unsupported AI fixture: " + requiredFixture);
        }
        return data;
    }

    private static void requireService(RequiredService service) {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(service);
    }
}
