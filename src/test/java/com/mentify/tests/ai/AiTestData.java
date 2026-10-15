package com.mentify.tests.ai;

import java.util.Map;
import java.util.UUID;

record AiTestData(UUID conversationId, UUID lessonId, UUID materialId, UUID courseId,
                  boolean writeEnabled, boolean providerEnabled) {

    static AiTestData fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new AiTestData(uuid(environment, "TEST_AI_CONVERSATION_ID"),
                uuid(environment, "TEST_AI_LESSON_ID"), uuid(environment, "TEST_AI_MATERIAL_ID"),
                uuid(environment, "TEST_AI_COURSE_ID"), Boolean.parseBoolean(
                        environment.getOrDefault("TEST_AI_WRITE_ENABLED", "false")),
                Boolean.parseBoolean(environment.getOrDefault("AI_TEST_MODE", "false")));
    }

    private static UUID uuid(Map<String, String> environment, String name) {
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
