package com.mentify.tests.assignment;

import java.util.Map;
import java.util.UUID;

record AssignmentAttachmentTestDataConfiguration(
        UUID assignmentId,
        UUID submissionId,
        UUID materialId,
        UUID otherMaterialId,
        boolean writeEnabled) {

    static AssignmentAttachmentTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new AssignmentAttachmentTestDataConfiguration(
                uuidValue(environment, "TEST_ATTACHMENT_ASSIGNMENT_ID"),
                uuidValue(environment, "TEST_ATTACHMENT_SUBMISSION_ID"),
                uuidValue(environment, "TEST_ATTACHMENT_MATERIAL_ID"),
                uuidValue(environment, "TEST_ATTACHMENT_OTHER_MATERIAL_ID"),
                Boolean.parseBoolean(environment.getOrDefault("TEST_ATTACHMENT_WRITE_ENABLED", "false")));
    }

    boolean readFixturesConfigured() {
        return assignmentId != null && submissionId != null && materialId != null;
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
