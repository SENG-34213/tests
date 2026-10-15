package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QuizAuthenticationIT extends QuizIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void protectedQuizRequestsRejectMissingAuthentication() {
        Map<String, Object> quiz = Map.of("title", "unauthenticated");
        assertEquals(401, quizClient.create(quiz, null).statusCode());
        assertEquals(401, quizClient.getTeacher(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.listTeacher(null).statusCode());
        assertEquals(401, quizClient.update(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, quizClient.delete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.publish(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.listForCourse(UNKNOWN_ID, null).statusCode());
        assertEquals(401, quizClient.getStudent(UNKNOWN_ID, null).statusCode());
        assertEquals(401, questionClient.create(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, questionClient.update(UNKNOWN_ID, UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, questionClient.delete(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
        assertEquals(401, attemptClient.start(UNKNOWN_ID, null).statusCode());
        assertEquals(401, attemptClient.saveAnswer(UNKNOWN_ID, quiz, null).statusCode());
        assertEquals(401, attemptClient.submit(UNKNOWN_ID, null).statusCode());
    }

    @Test
    void studentCannotUseTeacherQuizManagementEndpoints() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, quizClient.create(Map.of("title", "blocked"), student.accessToken()).statusCode());
            assertEquals(403, quizClient.getTeacher(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, quizClient.listTeacher(student.accessToken()).statusCode());
            assertEquals(403, quizClient.publish(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, questionClient.create(UNKNOWN_ID, Map.of(), student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCannotStartOrSubmitStudentAttempt() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, attemptClient.start(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(403, attemptClient.saveAnswer(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode());
            assertEquals(403, attemptClient.submit(UNKNOWN_ID, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
