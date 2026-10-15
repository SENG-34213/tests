package com.mentify.tests.attendance;

import java.util.Map;
import java.util.UUID;

record AttendanceTestDataConfiguration(UUID courseId, UUID studentId, boolean writeEnabled) {

    static AttendanceTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new AttendanceTestDataConfiguration(
                uuidValue(environment, "TEST_ATTENDANCE_COURSE_ID"),
                uuidValue(environment, "TEST_ATTENDANCE_STUDENT_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_ATTENDANCE_WRITE_ENABLED", "false")));
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
