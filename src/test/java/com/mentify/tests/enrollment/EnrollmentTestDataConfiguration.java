package com.mentify.tests.enrollment;

import java.util.Map;
import java.util.UUID;

record EnrollmentTestDataConfiguration(
        UUID studentId,
        UUID otherStudentId,
        UUID courseId,
        UUID alternateCourseId,
        UUID secondAlternateCourseId,
        UUID freeCourseId,
        UUID unownedCourseId,
        UUID enrollmentId,
        boolean writeEnabled) {

    static EnrollmentTestDataConfiguration fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new EnrollmentTestDataConfiguration(
                uuidValue(environment, "TEST_ENROLLMENT_STUDENT_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_OTHER_STUDENT_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_COURSE_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_ALTERNATE_COURSE_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_SECOND_ALTERNATE_COURSE_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_FREE_COURSE_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_UNOWNED_COURSE_ID"),
                uuidValue(environment, "TEST_ENROLLMENT_ID"),
                booleanValue(environment, "TEST_ENROLLMENT_WRITE_ENABLED"));
    }

    boolean readFixturesConfigured() {
        return studentId != null && otherStudentId != null && courseId != null;
    }

    boolean writeFixturesConfigured() {
        return readFixturesConfigured() && alternateCourseId != null
                && secondAlternateCourseId != null && writeEnabled;
    }

    boolean selfEnrollmentFixturesConfigured() {
        return studentId != null && freeCourseId != null && writeEnabled;
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

    private static boolean booleanValue(Map<String, String> environment, String name) {
        String value = environment.get(name);
        return value != null && Boolean.parseBoolean(value.trim());
    }
}
