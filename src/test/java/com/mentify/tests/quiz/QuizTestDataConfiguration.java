package com.mentify.tests.quiz;

import java.util.Map;
import java.util.UUID;

record QuizTestDataConfiguration(UUID courseId, UUID gradebookCourseId, UUID closedQuizId, boolean writeEnabled) {

    static QuizTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new QuizTestDataConfiguration(
                uuidValue(environment, "TEST_QUIZ_COURSE_ID"),
                uuidValue(environment, "TEST_QUIZ_GRADEBOOK_COURSE_ID"),
                uuidValue(environment, "TEST_QUIZ_CLOSED_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_QUIZ_WRITE_ENABLED", "false")));
    }

    boolean gradebookFixturesConfigured() {
        return gradebookCourseId != null && closedQuizId != null;
    }

    private static UUID uuidValue(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid UUID for " + name, exception);
        }
    }
}
