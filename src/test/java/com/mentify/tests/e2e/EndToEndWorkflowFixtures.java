package com.mentify.tests.e2e;

import java.util.Map;
import java.util.UUID;

record EndToEndWorkflowFixtures(
        UUID courseId,
        UUID moduleId,
        UUID lessonId,
        UUID assignmentId,
        UUID quizId,
        UUID gradedSubmissionId,
        UUID teacherCourseId,
        UUID teacherModuleId,
        UUID teacherLessonId,
        UUID teacherMaterialId,
        UUID teacherAssignmentId,
        UUID teacherSubmissionId) {

    static EndToEndWorkflowFixtures fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new EndToEndWorkflowFixtures(
                uuid(environment, "TEST_E2E_COURSE_ID"),
                uuid(environment, "TEST_E2E_MODULE_ID"),
                uuid(environment, "TEST_E2E_LESSON_ID"),
                uuid(environment, "TEST_E2E_ASSIGNMENT_ID"),
                uuid(environment, "TEST_E2E_QUIZ_ID"),
                uuid(environment, "TEST_E2E_GRADED_SUBMISSION_ID"),
                uuid(environment, "TEST_E2E_TEACHER_COURSE_ID"),
                uuid(environment, "TEST_E2E_TEACHER_MODULE_ID"),
                uuid(environment, "TEST_E2E_TEACHER_LESSON_ID"),
                uuid(environment, "TEST_E2E_TEACHER_MATERIAL_ID"),
                uuid(environment, "TEST_E2E_TEACHER_ASSIGNMENT_ID"),
                uuid(environment, "TEST_E2E_TEACHER_SUBMISSION_ID"));
    }

    boolean studentJourneyConfigured() {
        return courseId != null && moduleId != null && lessonId != null && assignmentId != null
                && quizId != null && gradedSubmissionId != null;
    }

    boolean teacherJourneyConfigured() {
        return teacherCourseId != null && teacherModuleId != null && teacherLessonId != null
                && teacherMaterialId != null && teacherAssignmentId != null && teacherSubmissionId != null;
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
