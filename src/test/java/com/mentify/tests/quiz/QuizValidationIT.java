package com.mentify.tests.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QuizValidationIT extends QuizIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void rejectsInvalidQuizRequests() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(400, quizClient.create(Map.of(), teacher.accessToken()).statusCode());
            assertEquals(400, quizClient.create(Map.of(
                    "courseId", UNKNOWN_ID, "title", " ", "durationMinutes", 0,
                    "passMark", -1, "maxAttempts", 0), teacher.accessToken()).statusCode());
            assertEquals(400, quizClient.create(Map.of(
                    "courseId", "not-a-uuid", "title", "invalid", "durationMinutes", 30,
                    "passMark", 50, "maxAttempts", 1), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void rejectsInvalidQuestionAndAnswerRequests() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(400, questionClient.create(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode());
            Map<String, Object> tooFew = new HashMap<>();
            tooFew.put("questionText", "invalid");
            tooFew.put("questionType", "SINGLE_CHOICE");
            tooFew.put("marks", 1);
            tooFew.put("questionOrder", 1);
            tooFew.put("options", List.of(Map.of("optionText", "A", "correct", true, "optionOrder", 1)));
            assertEquals(400, questionClient.create(UNKNOWN_ID, tooFew, teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }

        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, attemptClient.saveAnswer(UNKNOWN_ID, Map.of(), student.accessToken()).statusCode());
            assertEquals(400, attemptClient.saveAnswer(UNKNOWN_ID,
                    Map.of("questionId", "not-a-uuid", "selectedOptionId", "not-a-uuid"),
                    student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}
