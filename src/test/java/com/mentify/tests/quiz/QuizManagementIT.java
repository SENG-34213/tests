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
            assertEquals(201, created.jsonPath().getInt("statusCode"), created.asString());
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

            Response gradebookQuizzes = quizClient.teacherGradebookQuizzes(
                    config.courseId().toString(), teacher.accessToken());
            assertEquals(200, gradebookQuizzes.statusCode(), gradebookQuizzes.asString());
            assertNotNull(gradebookQuizzes.jsonPath().get("data"), gradebookQuizzes.asString());

            Response updated = quizClient.update(quizId, QuizTestDataFactory.update("MANAGEMENT"),
                    teacher.accessToken());
            assertEquals(200, updated.statusCode());
            assertEquals(45, updated.jsonPath().getInt("data.durationMinutes"));
            assertEquals(60.00F, updated.jsonPath().getFloat("data.passMark"), 0.01F);

            Response question = questionClient.create(quizId,
                    QuizTestDataFactory.question("primary", java.math.BigDecimal.valueOf(100)),
                    teacher.accessToken());
            assertEquals(201, question.statusCode());
            String questionId = question.jsonPath().getString("data.id");
            assertNotNull(questionId);
            assertEquals("MULTIPLE_CHOICE_SINGLE_ANSWER", question.jsonPath().getString("data.questionType"));
            assertEquals(4, question.jsonPath().getList("data.options").size());

            Response changedQuestion = questionClient.update(quizId, questionId,
                    QuizTestDataFactory.question("updated", java.math.BigDecimal.valueOf(100)), teacher.accessToken());
            assertEquals(200, changedQuestion.statusCode());
            assertTrue(changedQuestion.jsonPath().getString("data.questionText").contains("updated"));

            Response removable = questionClient.create(quizId, QuizTestDataFactory.question("removable"),
                    teacher.accessToken());
            String removableId = removable.jsonPath().getString("data.id");
            assertEquals(201, removable.statusCode());
            assertEquals(200, questionClient.delete(quizId, removableId, teacher.accessToken()).statusCode());

            Response published = quizClient.publish(quizId, teacher.accessToken());
            assertEquals(200, published.statusCode(), published.asString());
            assertTrue(published.jsonPath().getString("data.status").equalsIgnoreCase("PUBLISHED"));
        } finally {
            if (quizId != null) {
                quizClient.delete(quizId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void unknownQuizAndQuestionResourcesReturnNotFound() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(404, quizClient.getTeacher(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, quizClient.update(UNKNOWN_ID, QuizTestDataFactory.update("UNKNOWN"),
                    teacher.accessToken()).statusCode());
            assertEquals(404, quizClient.delete(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, quizClient.publish(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, quizClient.results(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, questionClient.create(UNKNOWN_ID, QuizTestDataFactory.question("UNKNOWN"),
                    teacher.accessToken()).statusCode());
            assertEquals(404, questionClient.update(UNKNOWN_ID, UNKNOWN_ID, QuizTestDataFactory.question("UNKNOWN"),
                    teacher.accessToken()).statusCode());
            assertEquals(404, questionClient.delete(UNKNOWN_ID, UNKNOWN_ID, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherCanReadClosedQuizResultsInGradebook() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.gradebookFixturesConfigured(),
                "Set TEST_QUIZ_GRADEBOOK_COURSE_ID and TEST_QUIZ_CLOSED_ID for the closed-quiz gradebook test");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = quizClient.teacherGradebookQuizzes(
                    config.gradebookCourseId().toString(), teacher.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertTrue(response.jsonPath().getList("data.quizId", String.class)
                    .contains(config.closedQuizId().toString()), response.asString());
            int quizIndex = response.jsonPath().getList("data.quizId", String.class)
                    .indexOf(config.closedQuizId().toString());
            assertNotNull(response.jsonPath().getString("data[" + quizIndex + "].title"), response.asString());
            assertNotNull(response.jsonPath().get("data[" + quizIndex + "].results"), response.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void anotherTeacherCannotReadOrManageQuizOwnedByTeacher() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for ownership tests");
        Assumptions.assumeTrue(hasSecondaryTeacherCredentials(),
                "Set distinct SECONDARY_TEACHER_TEST_USERNAME/PASSWORD credentials");

        AuthenticationSession owner = login(TestRole.TEACHER);
        AuthenticationSession otherTeacher = null;
        String quizId = null;
        try {
            Response created = quizClient.create(
                    QuizTestDataFactory.quiz(config.courseId(), "OWNERSHIP"), owner.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            quizId = created.jsonPath().getString("data.id");
            assertNotNull(quizId);

            otherTeacher = login(TestRole.SECONDARY_TEACHER);
            assertEquals(403, quizClient.getTeacher(quizId, otherTeacher.accessToken()).statusCode());
            assertEquals(403, quizClient.update(quizId, QuizTestDataFactory.update("OWNERSHIP"),
                    otherTeacher.accessToken()).statusCode());
            assertEquals(403, quizClient.delete(quizId, otherTeacher.accessToken()).statusCode());
        } finally {
            try {
                logout(otherTeacher);
            } finally {
                try {
                    if (quizId != null) {
                        quizClient.delete(quizId, owner.accessToken());
                    }
                } finally {
                    logout(owner);
                }
            }
        }
    }

    private static boolean hasSecondaryTeacherCredentials() {
        String username = System.getenv("SECONDARY_TEACHER_TEST_USERNAME");
        String password = System.getenv("SECONDARY_TEACHER_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
