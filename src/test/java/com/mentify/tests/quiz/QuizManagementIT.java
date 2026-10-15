package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class QuizManagementIT extends QuizIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void teacherManagesQuestionsAndPublishesQuiz() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for write tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String quizId = null;
        try {
            Response created = quizClient.create(QuizTestDataFactory.quiz(config.courseId(), "MANAGEMENT"),
                    teacher.accessToken());
            assertEquals(201, created.statusCode());
            assertTrue(created.jsonPath().getBoolean("success"));
            quizId = created.jsonPath().getString("data.id");
            assertNotNull(quizId);
            assertFalse(quizId.isBlank());
            assertEquals(config.courseId().toString(), created.jsonPath().getString("data.courseId"));
            assertEquals(30, created.jsonPath().getInt("data.durationMinutes"));
            assertEquals(2, created.jsonPath().getInt("data.maxAttempts"));

            Response retrieved = quizClient.getTeacher(quizId, teacher.accessToken());
            assertEquals(200, retrieved.statusCode());
            assertEquals(quizId, retrieved.jsonPath().getString("data.id"));
            assertTrue(retrieved.jsonPath().getString("data.status").equalsIgnoreCase("DRAFT"));

            Response listed = quizClient.listTeacher(teacher.accessToken());
            assertEquals(200, listed.statusCode());
            assertTrue(listed.jsonPath().getList("data.id", String.class).contains(quizId));

            Response updated = quizClient.update(quizId, QuizTestDataFactory.update("MANAGEMENT"),
                    teacher.accessToken());
            assertEquals(200, updated.statusCode());
            assertEquals(45, updated.jsonPath().getInt("data.durationMinutes"));
            assertEquals(60.00F, updated.jsonPath().getFloat("data.passMark"), 0.01F);

            Response question = questionClient.create(quizId, QuizTestDataFactory.question("primary"),
                    teacher.accessToken());
            assertEquals(201, question.statusCode());
            String questionId = question.jsonPath().getString("data.id");
            assertNotNull(questionId);
            assertEquals("SINGLE_CHOICE", question.jsonPath().getString("data.questionType"));
            assertEquals(4, question.jsonPath().getList("data.options").size());

            Response changedQuestion = questionClient.update(quizId, questionId,
                    QuizTestDataFactory.question("updated"), teacher.accessToken());
            assertEquals(200, changedQuestion.statusCode());
            assertTrue(changedQuestion.jsonPath().getString("data.questionText").contains("updated"));

            Response removable = questionClient.create(quizId, QuizTestDataFactory.question("removable"),
                    teacher.accessToken());
            String removableId = removable.jsonPath().getString("data.id");
            assertEquals(201, removable.statusCode());
            assertTrue(questionClient.delete(quizId, removableId, teacher.accessToken()).statusCode() < 300);

            Response published = quizClient.publish(quizId, teacher.accessToken());
            assertEquals(200, published.statusCode());
            assertTrue(published.jsonPath().getString("data.status").equalsIgnoreCase("PUBLISHED"));
        } finally {
            if (quizId != null) {
                quizClient.delete(quizId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void rejectsUnknownQuizAndQuestionIdsWithoutServerError() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertTrue(quizClient.getTeacher(UNKNOWN_ID, teacher.accessToken()).statusCode() < 500);
            assertTrue(quizClient.update(UNKNOWN_ID, QuizTestDataFactory.update("UNKNOWN"),
                    teacher.accessToken()).statusCode() < 500);
            assertTrue(questionClient.create(UNKNOWN_ID, QuizTestDataFactory.question("UNKNOWN"),
                    teacher.accessToken()).statusCode() < 500);
            assertTrue(questionClient.update(UNKNOWN_ID, UNKNOWN_ID, Map.of(),
                    teacher.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }
}
