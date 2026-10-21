package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.material.MaterialClient;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseContentWorkflowIT extends CourseIntegrationTestSupport {

    private TeacherPortalTestData data;

    @BeforeEach
    void requireOwnedCourseFixture() {
        data = TeacherPortalTestData.fromEnvironment();
        Assumptions.assumeTrue(data.courseId() != null,
                "Set TEST_TEACHER_PORTAL_COURSE_ID to an existing course owned by the test teacher");
    }

    @Test
    void shouldCreateReadUpdateAndDeleteModuleAndLesson() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String courseId = data.courseId().toString();
        String moduleId = null;
        String lessonId = null;
        try {
            Response createModule = courseClient.createModule(courseId, moduleRequest("Module"),
                    teacher.accessToken());
            assertEquals(201, createModule.statusCode(), createModule.asString());
            moduleId = resourceId(createModule);

            Response listModules = courseClient.modules(courseId, teacher.accessToken());
            assertEquals(200, listModules.statusCode(), listModules.asString());
            assertTrue(listModules.jsonPath().getList("data.id").contains(moduleId), listModules.asString());

            Response updateModule = courseClient.updateModule(courseId, moduleId,
                    moduleRequest("Updated module"), teacher.accessToken());
            assertEquals(200, updateModule.statusCode(), updateModule.asString());
            assertEquals("Updated module", updateModule.jsonPath().getString("data.title"));

            Response readModule = courseClient.module(courseId, moduleId, teacher.accessToken());
            assertEquals(200, readModule.statusCode(), readModule.asString());

            Response createLesson = courseClient.createLesson(courseId, moduleId, lessonRequest("Lesson"),
                    teacher.accessToken());
            assertEquals(201, createLesson.statusCode(), createLesson.asString());
            lessonId = resourceId(createLesson);

            Response listLessons = courseClient.lessons(courseId, moduleId, teacher.accessToken());
            assertEquals(200, listLessons.statusCode(), listLessons.asString());
            assertTrue(listLessons.jsonPath().getList("data.id").contains(lessonId), listLessons.asString());

            Response updateLesson = courseClient.updateLesson(courseId, moduleId, lessonId,
                    lessonRequest("Updated lesson"), teacher.accessToken());
            assertEquals(200, updateLesson.statusCode(), updateLesson.asString());
            assertEquals("Updated lesson", updateLesson.jsonPath().getString("data.title"));
            assertEquals(200, courseClient.lesson(courseId, moduleId, lessonId,
                    teacher.accessToken()).statusCode());

            assertEquals(200, courseClient.deleteLesson(courseId, moduleId, lessonId,
                    teacher.accessToken()).statusCode());
            lessonId = null;
            assertEquals(200, courseClient.deleteModule(courseId, moduleId, teacher.accessToken()).statusCode());
            moduleId = null;
        } finally {
            if (lessonId != null && moduleId != null) {
                courseClient.deleteLesson(courseId, moduleId, lessonId, teacher.accessToken());
            }
            if (moduleId != null) {
                courseClient.deleteModule(courseId, moduleId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void shouldCreateReadUpdateAndDeleteLearningMaterialReferences() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String courseId = data.courseId().toString();
        String moduleId = null;
        String lessonId = null;
        String materialId = null;
        try {
            Response module = courseClient.createModule(courseId, moduleRequest("Material module"),
                    teacher.accessToken());
            assertEquals(201, module.statusCode(), module.asString());
            moduleId = resourceId(module);

            Response lesson = courseClient.createLesson(courseId, moduleId, lessonRequest("Material lesson"),
                    teacher.accessToken());
            assertEquals(201, lesson.statusCode(), lesson.asString());
            lessonId = resourceId(lesson);

            Response create = courseClient.createLearningMaterial(courseId, moduleId,
                    materialRequest("Course reference", lessonId, "/initial.pdf"), teacher.accessToken());
            assertEquals(201, create.statusCode(), create.asString());
            materialId = resourceId(create);

            Response list = courseClient.learningMaterials(courseId, moduleId, teacher.accessToken());
            assertEquals(200, list.statusCode(), list.asString());
            assertTrue(list.jsonPath().getList("data.id").contains(materialId), list.asString());
            Response lessonMaterials = courseClient.lessonMaterials(courseId, moduleId, lessonId,
                    teacher.accessToken());
            assertEquals(200, lessonMaterials.statusCode(), lessonMaterials.asString());
            assertTrue(lessonMaterials.jsonPath().getList("data.id").contains(materialId), lessonMaterials.asString());
            assertEquals(200, courseClient.learningMaterial(courseId, moduleId, materialId,
                    teacher.accessToken()).statusCode());

            Response update = courseClient.updateLearningMaterial(courseId, moduleId, materialId,
                    materialRequest("Updated reference", lessonId, "/updated.pdf"), teacher.accessToken());
            assertEquals(200, update.statusCode(), update.asString());
            assertEquals("Updated reference", update.jsonPath().getString("data.title"));

            assertEquals(200, courseClient.deleteLearningMaterial(courseId, moduleId, materialId,
                    teacher.accessToken()).statusCode());
            materialId = null;
            assertEquals(200, courseClient.deleteLesson(courseId, moduleId, lessonId,
                    teacher.accessToken()).statusCode());
            lessonId = null;
            assertEquals(200, courseClient.deleteModule(courseId, moduleId, teacher.accessToken()).statusCode());
            moduleId = null;
        } finally {
            if (materialId != null && moduleId != null) {
                courseClient.deleteLearningMaterial(courseId, moduleId, materialId, teacher.accessToken());
            }
            if (lessonId != null && moduleId != null) {
                courseClient.deleteLesson(courseId, moduleId, lessonId, teacher.accessToken());
            }
            if (moduleId != null) {
                courseClient.deleteModule(courseId, moduleId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void shouldUploadModuleAndLessonMaterialsAndAssociateThemAcrossServices() throws Exception {
        TeacherPortalTestData uploadData = TeacherPortalTestData.fromEnvironment();
        Assumptions.assumeTrue(Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_COURSE_CONTENT_UPLOAD_WRITE_ENABLED", "false"))
                        && uploadData.courseId() != null,
                "Set TEST_COURSE_CONTENT_UPLOAD_WRITE_ENABLED=true and TEST_TEACHER_PORTAL_COURSE_ID");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(RequiredService.MATERIAL_SERVICE);

        AuthenticationSession teacher = login(TestRole.TEACHER);
        MaterialClient materials = new MaterialClient(apiClient);
        String courseId = uploadData.courseId().toString();
        String moduleId = null;
        String lessonId = null;
        String moduleReferenceId = null;
        String lessonReferenceId = null;
        String moduleMaterialId = null;
        String lessonMaterialId = null;
        Path file = Files.createTempFile("mentify-course-content-", ".txt");
        try {
            Files.writeString(file, "Synthetic cross-service course material " + UUID.randomUUID(),
                    StandardCharsets.UTF_8);
            Response module = courseClient.createModule(courseId, moduleRequest("Upload module"),
                    teacher.accessToken());
            assertEquals(201, module.statusCode(), module.asString());
            moduleId = resourceId(module);
            Response lesson = courseClient.createLesson(courseId, moduleId, lessonRequest("Upload lesson"),
                    teacher.accessToken());
            assertEquals(201, lesson.statusCode(), lesson.asString());
            lessonId = resourceId(lesson);

            Response moduleUpload = courseClient.uploadModuleMaterial(courseId, moduleId,
                    "Module upload " + UUID.randomUUID(), "Synthetic module-level material",
                    file.toFile(), teacher.accessToken());
            moduleReferenceId = moduleUpload.jsonPath().getString("data.id");
            moduleMaterialId = moduleUpload.jsonPath().getString("data.materialId");
            assertEquals(201, moduleUpload.statusCode(), moduleUpload.asString());
            assertNotNull(moduleMaterialId, moduleUpload.asString());

            Response lessonUpload = courseClient.uploadLessonMaterial(courseId, moduleId, lessonId,
                    "Lesson upload " + UUID.randomUUID(), "Synthetic lesson-level material",
                    file.toFile(), teacher.accessToken());
            lessonReferenceId = lessonUpload.jsonPath().getString("data.id");
            lessonMaterialId = lessonUpload.jsonPath().getString("data.materialId");
            assertEquals(201, lessonUpload.statusCode(), lessonUpload.asString());
            assertNotNull(lessonMaterialId, lessonUpload.asString());

            Response lessonMaterials = courseClient.lessonMaterials(courseId, moduleId, lessonId,
                    teacher.accessToken());
            assertEquals(200, lessonMaterials.statusCode(), lessonMaterials.asString());
            assertTrue(lessonMaterials.jsonPath().getList("data.id").contains(lessonReferenceId),
                    lessonMaterials.asString());
            assertEquals(200, materials.get(moduleMaterialId, teacher.accessToken()).statusCode());
            assertEquals(courseId, materials.get(lessonMaterialId, teacher.accessToken())
                    .jsonPath().getString("data.courseId"));
        } finally {
            if (moduleId != null && lessonReferenceId != null) {
                courseClient.deleteLearningMaterial(courseId, moduleId, lessonReferenceId, teacher.accessToken());
            }
            if (moduleId != null && moduleReferenceId != null) {
                courseClient.deleteLearningMaterial(courseId, moduleId, moduleReferenceId, teacher.accessToken());
            }
            if (lessonMaterialId != null) {
                materials.delete(lessonMaterialId, teacher.accessToken());
            }
            if (moduleMaterialId != null) {
                materials.delete(moduleMaterialId, teacher.accessToken());
            }
            if (lessonId != null && moduleId != null) {
                courseClient.deleteLesson(courseId, moduleId, lessonId, teacher.accessToken());
            }
            if (moduleId != null) {
                courseClient.deleteModule(courseId, moduleId, teacher.accessToken());
            }
            Files.deleteIfExists(file);
            logout(teacher);
        }
    }

    private static String resourceId(Response response) {
        String id = response.jsonPath().getString("data.id");
        assertNotNull(id, response.asString());
        return id;
    }

    private static Map<String, Object> moduleRequest(String title) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("title", title + " " + UUID.randomUUID());
        request.put("description", "TEST-028 isolated course content workflow");
        request.put("sequenceOrder", Math.floorMod(UUID.randomUUID().hashCode(), 100_000) + 1);
        request.put("startDate", LocalDate.now().toString());
        request.put("endDate", LocalDate.now().plusDays(7).toString());
        request.put("isVisible", true);
        return request;
    }

    private static Map<String, Object> lessonRequest(String title) {
        return Map.of("title", title + " " + UUID.randomUUID(),
                "description", "TEST-028 isolated lesson workflow",
                "durationMinutes", 30,
                "contentMarkdown", "Test lesson content",
                "contentType", "MARKDOWN",
                "isVisible", true);
    }

    private static Map<String, Object> materialRequest(String title, String lessonId, String path) {
        return Map.of("title", title + " " + UUID.randomUUID(),
                "type", "PDF",
                "fileUrl", "https://example.invalid" + path,
                "lessonId", lessonId);
    }
}
