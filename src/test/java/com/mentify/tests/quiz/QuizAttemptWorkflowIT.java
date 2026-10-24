package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.RequiredService;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class QuizAttemptWorkflowIT extends QuizIntegrationTestSupport {

    @Test
    void enrolledStudentCanAccessPublishedQuizAndCompleteAttempt() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for workflow tests");
        requireAdditionalService(RequiredService.USER_SERVICE);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession student = null;
        String quizId = null;
        try {
            Response quiz = quizClient.create(QuizTestDataFactory.quiz(
                    config.courseId(), "WORKFLOW", BigDecimal.valueOf(5)),
                    teacher.accessToken());
            assertEquals(201, quiz.statusCode());
            quizId = quiz.jsonPath().getString("data.id");
            Response question = questionClient.create(quizId, QuizTestDataFactory.question("workflow"),
                    teacher.accessToken());
            assertEquals(201, question.statusCode());
            String questionId = question.jsonPath().getString("data.id");
            String optionId = question.jsonPath().getString("data.options[0].id");
            assertNotNull(optionId);
            Response published = quizClient.publish(quizId, teacher.accessToken());
            assertEquals(200, published.statusCode(), published.asString());
            assertEquals("PUBLISHED", published.jsonPath().getString("data.status"));

            student = login(TestRole.STUDENT);
            Response courseQuizzes = quizClient.listForCourse(config.courseId().toString(), student.accessToken());
            assertEquals(200, courseQuizzes.statusCode());
            assertTrue(courseQuizzes.jsonPath().getList("data.id", String.class).contains(quizId));

            Response studentQuiz = quizClient.getStudent(quizId, student.accessToken());
            assertEquals(200, studentQuiz.statusCode());
            assertEquals(quizId, studentQuiz.jsonPath().getString("data.id"));
            assertEquals(1, studentQuiz.jsonPath().getList("data.questions").size());
            Map<String, Object> studentOption = studentQuiz.jsonPath().getMap("data.questions[0].options[0]");
            assertNotNull(studentOption);
            assertFalse(studentOption.containsKey("correct"), "Student quiz options must not reveal the answer key");
            assertFalse(studentOption.containsKey("isCorrect"), "Student quiz options must not reveal the answer key");

            Response started = attemptClient.start(quizId, student.accessToken());
            assertEquals(201, started.statusCode(), started.asString());
            String attemptId = started.jsonPath().getString("data.attemptId");
            assertNotNull(attemptId);
            assertEquals(quizId, started.jsonPath().getString("data.quizId"));
            assertEquals("IN_PROGRESS", started.jsonPath().getString("data.status"));
            assertEquals(1, started.jsonPath().getInt("data.attemptNumber"));
            assertEquals(409, attemptClient.start(quizId, student.accessToken()).statusCode(),
                    "A student cannot start a second attempt while one is active");

            Response answer = attemptClient.saveAnswer(attemptId,
                    QuizTestDataFactory.answer(questionId, optionId), student.accessToken());
            assertEquals(200, answer.statusCode(), answer.asString());
            assertEquals(questionId, answer.jsonPath().getString("data.questionId"));
            assertEquals(optionId, answer.jsonPath().getString("data.selectedOptionId"));
            assertNotNull(answer.jsonPath().getString("data.answeredAt"));

            Response submitted = attemptClient.submit(attemptId, student.accessToken());
            assertEquals(200, submitted.statusCode(), submitted.asString());
            assertEquals(attemptId, submitted.jsonPath().getString("data.attemptId"));
            assertEquals(quizId, submitted.jsonPath().getString("data.quizId"));
            assertEquals(10.0, submitted.jsonPath().getDouble("data.score"), 0.001);
            assertEquals(10.0, submitted.jsonPath().getDouble("data.totalMarks"), 0.001);
            assertEquals(100.0, submitted.jsonPath().getDouble("data.percentage"), 0.001);
            assertTrue(submitted.jsonPath().getBoolean("data.passed"));
            assertNotNull(submitted.jsonPath().getString("data.submittedAt"));
            assertEquals(409, attemptClient.submit(attemptId, student.accessToken()).statusCode());
            assertEquals(409, attemptClient.saveAnswer(attemptId,
                    QuizTestDataFactory.answer(questionId, optionId), student.accessToken()).statusCode());

            Response secondAttempt = attemptClient.start(quizId, student.accessToken());
            assertEquals(201, secondAttempt.statusCode(), secondAttempt.asString());
            String secondAttemptId = secondAttempt.jsonPath().getString("data.attemptId");
            assertNotNull(secondAttemptId);
            assertEquals(2, secondAttempt.jsonPath().getInt("data.attemptNumber"));
            Response submittedUnansweredAttempt = attemptClient.submit(secondAttemptId, student.accessToken());
            assertEquals(200, submittedUnansweredAttempt.statusCode(), submittedUnansweredAttempt.asString());
            assertEquals(0.0, submittedUnansweredAttempt.jsonPath().getDouble("data.score"), 0.001);
            assertEquals(0.0, submittedUnansweredAttempt.jsonPath().getDouble("data.percentage"), 0.001);
            assertFalse(submittedUnansweredAttempt.jsonPath().getBoolean("data.passed"));
            assertEquals(409, attemptClient.start(quizId, student.accessToken()).statusCode(),
                    "A student cannot exceed the quiz's configured attempt limit");

            Response teacherResults = quizClient.results(quizId, teacher.accessToken());
            assertEquals(200, teacherResults.statusCode(), teacherResults.asString());
            assertEquals(quizId, teacherResults.jsonPath().getString("data.quizId"));
            assertEquals(10.0, teacherResults.jsonPath().getDouble("data.totalMarks"), 0.001);
            assertEquals(1, teacherResults.jsonPath().getList("data.results").size(), teacherResults.asString());
            assertEquals(0.0, teacherResults.jsonPath().getDouble("data.results[0].marksObtained"), 0.001);
            assertEquals(0.0, teacherResults.jsonPath().getDouble("data.results[0].percentage"), 0.001);
            assertFalse(teacherResults.jsonPath().getBoolean("data.results[0].passed"));
            assertNotNull(teacherResults.jsonPath().getString("data.results[0].studentName"));
            assertNotNull(teacherResults.jsonPath().getString("data.results[0].submittedAt"));
        } finally {
            try {
                logout(student);
            } finally {
                try {
                    if (quizId != null) {
                        quizClient.delete(quizId, teacher.accessToken());
                    }
                } finally {
                    logout(teacher);
                }
            }
        }
    }
}
