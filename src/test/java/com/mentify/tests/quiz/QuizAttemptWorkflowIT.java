package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class QuizAttemptWorkflowIT extends QuizIntegrationTestSupport {

    @Test
    void enrolledStudentCanAccessPublishedQuizAndCompleteAttempt() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for workflow tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession student = null;
        String quizId = null;
        try {
            Response quiz = quizClient.create(QuizTestDataFactory.quiz(config.courseId(), "WORKFLOW"),
                    teacher.accessToken());
            assertEquals(201, quiz.statusCode());
            quizId = quiz.jsonPath().getString("data.id");
            Response question = questionClient.create(quizId, QuizTestDataFactory.question("workflow"),
                    teacher.accessToken());
            assertEquals(201, question.statusCode());
            String questionId = question.jsonPath().getString("data.id");
            String optionId = question.jsonPath().getString("data.options[0].id");
            assertNotNull(optionId);
            assertTrue(quizClient.publish(quizId, teacher.accessToken()).statusCode() < 300);

            student = login(TestRole.STUDENT);
            Response courseQuizzes = quizClient.listForCourse(config.courseId().toString(), student.accessToken());
            assertEquals(200, courseQuizzes.statusCode());
            assertTrue(courseQuizzes.jsonPath().getList("data.id", String.class).contains(quizId));

            Response studentQuiz = quizClient.getStudent(quizId, student.accessToken());
            assertEquals(200, studentQuiz.statusCode());
            assertEquals(quizId, studentQuiz.jsonPath().getString("data.id"));
            assertFalse(studentQuiz.jsonPath().getMap("data").containsKey("correctAnswer"));

            Response started = attemptClient.start(quizId, student.accessToken());
            assertEquals(201, started.statusCode());
            String attemptId = started.jsonPath().getString("data.attemptId");
            if (attemptId == null) {
                attemptId = started.jsonPath().getString("data.id");
            }
            assertNotNull(attemptId);
            assertEquals(quizId, started.jsonPath().getString("data.quizId"));

            Response answer = attemptClient.saveAnswer(attemptId,
                    QuizTestDataFactory.answer(questionId, optionId), student.accessToken());
            assertTrue(answer.statusCode() < 300);
            assertEquals(questionId, answer.jsonPath().getString("data.questionId"));

            Response submitted = attemptClient.submit(attemptId, student.accessToken());
            assertTrue(submitted.statusCode() < 300);
            assertTrue(submitted.jsonPath().getString("data.status").toUpperCase().contains("SUBMIT"));
            assertTrue(attemptClient.submit(attemptId, student.accessToken()).statusCode() < 500);
        } finally {
            logout(student);
            if (quizId != null) {
                quizClient.delete(quizId, teacher.accessToken());
            }
            logout(teacher);
        }
    }
}
