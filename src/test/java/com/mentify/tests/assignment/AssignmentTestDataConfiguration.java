package com.mentify.tests.assignment;

import java.util.Map;
import java.util.UUID;

record AssignmentTestDataConfiguration(
        UUID courseId,
        UUID moduleId,
        UUID studentId,
        UUID assignmentId,
        UUID lateSubmissionId,
        UUID attemptLimitAssignmentId,
        boolean writeEnabled) {

    static AssignmentTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new AssignmentTestDataConfiguration(
                uuidValue(environment, "TEST_ASSIGNMENT_COURSE_ID"),
                uuidValue(environment, "TEST_ASSIGNMENT_MODULE_ID"),
                uuidValue(environment, "TEST_ASSIGNMENT_STUDENT_ID"),
                uuidValue(environment, "TEST_ASSIGNMENT_ID"),
                uuidValue(environment, "TEST_ASSIGNMENT_LATE_SUBMISSION_ID"),
                uuidValue(environment, "TEST_ASSIGNMENT_ATTEMPT_LIMIT_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_ASSIGNMENT_WRITE_ENABLED", "false")));
    }

    boolean readFixturesConfigured() {
        return courseId != null && moduleId != null && studentId != null && assignmentId != null;
    }

    boolean writeFixturesConfigured() {
        return readFixturesConfigured() && writeEnabled;
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
