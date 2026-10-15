package com.mentify.tests.communication;

import java.util.Map;
import java.util.UUID;

record CommunicationTestDataConfiguration(UUID courseId, UUID studentId, UUID groupId, boolean writeEnabled) {

    static CommunicationTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new CommunicationTestDataConfiguration(
                uuidValue(environment, "TEST_COMMUNICATION_COURSE_ID"),
                uuidValue(environment, "TEST_COMMUNICATION_STUDENT_ID"),
                uuidValue(environment, "TEST_COMMUNICATION_GROUP_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_COMMUNICATION_WRITE_ENABLED", "false")));
    }

    boolean fixturesConfigured() {
        return courseId != null && studentId != null;
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
