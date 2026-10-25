package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class QuizAuthenticationIT extends QuizIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void protectedQuizRequestsRejectMissingAuthentication() {
        Map<String, Object> quiz = Map.of("title", "unauthenticated");
        assertEquals(401, quizClient.create(quiz, null).statusCode());
        assertEquals(401, quizClient.getTeacher(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.results(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.listTeacher(null).statusCode());
        assertEquals(401, quizClient.update(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, quizClient.delete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.publish(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.listForCourse(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.getStudent(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.teacherGradebookQuizzes(UNKNOWN_ID, null).statusCode());
        assertEquals(401, questionClient.create(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, questionClient.update(UNKNOWN_ID, UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, questionClient.delete(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, attemptClient.start(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attemptClient.saveAnswer(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, attemptClient.submit(UNKNOWN_ID, null).statusCode());
    }

    @Test
    void studentCannotUseTeacherQuizManagementEndpoints() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for role checks");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String quizId = null;
        AuthenticationSession student = null;
        try {
            var created = quizClient.create(QuizTestDataFactory.quiz(config.courseId(), "ROLE-STUDENT"),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            quizId = created.jsonPath().getString("data.id");
            Assumptions.assumeTrue(quizId != null && !quizId.isBlank(),
                    "A valid quiz fixture is required for role authorization checks");
            var question = questionClient.create(quizId, QuizTestDataFactory.question("ROLE-STUDENT"),
                    teacher.accessToken());
            assertEquals(201, question.statusCode(), question.asString());
            String questionId = question.jsonPath().getString("data.id");

            student = login(TestRole.STUDENT);
            assertEquals(403, quizClient.create(QuizTestDataFactory.quiz(config.courseId(), "BLOCKED"),
                    student.accessToken()).statusCode());
            assertEquals(403, quizClient.getTeacher(quizId, student.accessToken()).statusCode());
            assertEquals(403, quizClient.listTeacher(student.accessToken()).statusCode());
            assertEquals(403, quizClient.results(quizId, student.accessToken()).statusCode());
            assertEquals(403, quizClient.teacherGradebookQuizzes(config.courseId().toString(),
                    student.accessToken()).statusCode());
            assertEquals(403, quizClient.publish(quizId, student.accessToken()).statusCode());
            assertEquals(403, questionClient.create(quizId, QuizTestDataFactory.question("BLOCKED"),
                    student.accessToken()).statusCode());
            assertEquals(403, questionClient.update(quizId, questionId,
                    QuizTestDataFactory.question("BLOCKED"), student.accessToken()).statusCode());
            assertEquals(403, questionClient.delete(quizId, questionId, student.accessToken()).statusCode());
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

    @Test
    void teacherCannotStartOrSubmitStudentAttempt() {
        QuizTestDataConfiguration config = QuizTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(config.writeEnabled() && config.courseId() != null,
                "Set TEST_QUIZ_WRITE_ENABLED=true and TEST_QUIZ_COURSE_ID for role checks");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String quizId = null;
        try {
            var created = quizClient.create(QuizTestDataFactory.quiz(config.courseId(), "ROLE-TEACHER"),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            quizId = created.jsonPath().getString("data.id");
            Assumptions.assumeTrue(quizId != null && !quizId.isBlank(),
                    "A valid quiz fixture is required for role authorization checks");
            UUID unknownAttemptId = UUID.randomUUID();
            UUID unknownQuestionId = UUID.randomUUID();
            UUID unknownOptionId = UUID.randomUUID();
            assertEquals(403, attemptClient.start(quizId, teacher.accessToken()).statusCode());
            assertEquals(403, attemptClient.saveAnswer(unknownAttemptId.toString(),
                    QuizTestDataFactory.answer(unknownQuestionId.toString(), unknownOptionId.toString()),
                    teacher.accessToken()).statusCode());
            assertEquals(403, attemptClient.submit(unknownAttemptId.toString(), teacher.accessToken()).statusCode());
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

    @Test
    void teacherGradebookQuizRouteValidatesRequiredCourseId() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(400, apiClient.get("/api/v1/teacher/gradebook/quizzes", teacher.accessToken(),
                    Map.of(), Map.of()).statusCode());
            assertEquals(400, apiClient.get("/api/v1/teacher/gradebook/quizzes", teacher.accessToken(),
                    Map.of("courseId", "not-a-uuid"), Map.of()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
