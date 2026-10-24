package com.mentify.tests.quiz;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class QuizTestDataFactory {

    private QuizTestDataFactory() {
    }

    static Map<String, Object> quiz(UUID courseId, String suffix) {
        return quiz(courseId, suffix, BigDecimal.valueOf(50.00));
    }

    static Map<String, Object> quiz(UUID courseId, String suffix, BigDecimal passMark) {
        return Map.of(
                "courseId", courseId.toString(),
                "title", "TEST-QUIZ-" + suffix + "-" + UUID.randomUUID(),
                "description", "Synthetic integration test quiz",
                "durationMinutes", 30,
                "passMark", passMark,
                "startTime", LocalDateTime.now().minusMinutes(1).withNano(0).toString(),
                "endTime", LocalDateTime.now().plusDays(7).withNano(0).toString(),
                "maxAttempts", 2,
                "showResultImmediately", true,
                "creationMethod", "MANUAL");
    }

    static Map<String, Object> update(String suffix) {
        return Map.of(
                "title", "TEST-QUIZ-UPDATED-" + suffix + "-" + UUID.randomUUID(),
                "description", "Updated synthetic quiz",
                "durationMinutes", 45,
                "passMark", BigDecimal.valueOf(60.00),
                "maxAttempts", 2,
                "showResultImmediately", true);
    }

    static Map<String, Object> question(String suffix) {
        return question(suffix, BigDecimal.TEN);
    }

    static Map<String, Object> question(String suffix, BigDecimal marks) {
        return Map.of(
                "questionText", "Which answer is correct " + suffix + "?",
                "questionType", "MULTIPLE_CHOICE_SINGLE_ANSWER",
                "marks", marks,
                "questionOrder", 1,
                "options", List.of(
                        option("Option A", true, 1), option("Option B", false, 2),
                        option("Option C", false, 3), option("Option D", false, 4)));
    }

    static Map<String, Object> answer(String questionId, String optionId) {
        return Map.of("questionId", questionId, "selectedOptionId", optionId);
    }

    static Map<String, Object> option(String text, boolean correct, int order) {
        return Map.of("optionText", text, "correct", correct, "optionOrder", order);
    }
}
