package com.mentify.tests.course;

import java.util.Map;
import java.util.UUID;

record TeacherPortalTestData(UUID courseId, UUID enrolledStudentId, UUID unownedCourseId,
                             UUID unownedModuleId, UUID unownedLessonId,
                             UUID unownedLearningMaterialId) {

    static TeacherPortalTestData fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new TeacherPortalTestData(
                uuidValue(environment, "TEST_TEACHER_PORTAL_COURSE_ID"),
                uuidValue(environment, "TEST_TEACHER_PORTAL_STUDENT_ID"),
                uuidValue(environment, "TEST_TEACHER_PORTAL_UNOWNED_COURSE_ID"),
                uuidValue(environment, "TEST_TEACHER_PORTAL_UNOWNED_MODULE_ID"),
                uuidValue(environment, "TEST_TEACHER_PORTAL_UNOWNED_LESSON_ID"),
                uuidValue(environment, "TEST_TEACHER_PORTAL_UNOWNED_LEARNING_MATERIAL_ID"));
    }

    boolean configured() {
        return courseId != null && enrolledStudentId != null;
    }

    boolean unownedContentConfigured() {
        return unownedCourseId != null && unownedModuleId != null
                && unownedLessonId != null && unownedLearningMaterialId != null;
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
