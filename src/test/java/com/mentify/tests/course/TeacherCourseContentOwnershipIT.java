package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class TeacherCourseContentOwnershipIT extends CourseIntegrationTestSupport {

    @Test
    void teacherCannotMutateAnotherTeachersCourseContent() {
        TeacherPortalTestData data = TeacherPortalTestData.fromEnvironment();
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_COURSE_CONTENT_OWNERSHIP_WRITE_ENABLED", "false"))
                        && data.unownedContentConfigured(),
                "Enable TEST_COURSE_CONTENT_OWNERSHIP_WRITE_ENABLED and configure valid foreign course, module, "
                        + "lesson, and learning-material reference IDs in a disposable non-production environment");

        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String courseId = data.unownedCourseId().toString();
            String moduleId = data.unownedModuleId().toString();
            String lessonId = data.unownedLessonId().toString();
            String materialId = data.unownedLearningMaterialId().toString();

            assertEquals(403, courseClient.createModule(courseId, moduleRequest(), teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.updateModule(courseId, moduleId, moduleRequest(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.deleteModule(courseId, moduleId, teacher.accessToken()).statusCode());

            assertEquals(403, courseClient.createLesson(courseId, moduleId, lessonRequest(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.updateLesson(courseId, moduleId, lessonId, lessonRequest(),
                    teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.deleteLesson(courseId, moduleId, lessonId,
                    teacher.accessToken()).statusCode());

            assertEquals(403, courseClient.createLearningMaterial(courseId, moduleId,
                    materialRequest(lessonId), teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.updateLearningMaterial(courseId, moduleId, materialId,
                    materialRequest(lessonId), teacher.accessToken()).statusCode());
            assertEquals(403, courseClient.deleteLearningMaterial(courseId, moduleId, materialId,
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    private static Map<String, Object> moduleRequest() {
        return Map.of("title", "Ownership probe " + UUID.randomUUID(),
                "description", "Denied cross-owner mutation probe",
                "sequenceOrder", 999_999,
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(1).toString(),
                "isVisible", true);
    }

    private static Map<String, Object> lessonRequest() {
        return Map.of("title", "Ownership probe " + UUID.randomUUID(),
                "description", "Denied cross-owner mutation probe",
                "durationMinutes", 10,
                "contentMarkdown", "Synthetic ownership test",
                "contentType", "MARKDOWN",
                "isVisible", true);
    }

    private static Map<String, Object> materialRequest(String lessonId) {
        return Map.of("title", "Ownership probe " + UUID.randomUUID(),
                "type", "PDF",
                "fileUrl", "https://example.invalid/ownership-probe.pdf",
                "lessonId", lessonId);
    }
}
