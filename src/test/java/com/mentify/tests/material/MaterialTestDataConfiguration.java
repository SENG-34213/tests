package com.mentify.tests.material;

import java.util.Map;
import java.util.UUID;

record MaterialTestDataConfiguration(
        UUID courseId,
        UUID materialId,
        boolean writeEnabled) {

    static MaterialTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new MaterialTestDataConfiguration(
                uuidValue(environment, "TEST_MATERIAL_COURSE_ID"),
                uuidValue(environment, "TEST_MATERIAL_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_MATERIAL_WRITE_ENABLED", "false")));
    }

    boolean readFixturesConfigured() {
        return courseId != null && materialId != null;
    }

    boolean writeFixturesConfigured() {
        return courseId != null && writeEnabled;
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
