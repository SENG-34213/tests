package com.mentify.tests.course;

import java.util.Map;
import java.util.UUID;

record CourseTestDataConfiguration(UUID gradeId, UUID teacherId) {

    static CourseTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new CourseTestDataConfiguration(
                uuidValue(environment, "TEST_COURSE_GRADE_ID"),
                uuidValue(environment, "TEST_COURSE_TEACHER_ID"));
    }

    boolean configured() {
        return gradeId != null && teacherId != null;
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
