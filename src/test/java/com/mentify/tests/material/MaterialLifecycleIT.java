package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class MaterialLifecycleIT extends MaterialIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void teacherCanUploadRetrieveListDownloadUpdateArchiveAndDeleteMaterial() {
        MaterialTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String materialId = null;
        try (MaterialTestFileFactory.TestFile file = MaterialTestFileFactory.createTextFile()) {
            String title = "TEST-013-" + java.util.UUID.randomUUID();
            Response created = materialClient.upload(materialFields(data, title),
                    file.path().toFile(), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals("success", created.jsonPath().getString("status"));
            materialId = created.jsonPath().getString("data.id");
            assertNotNull(materialId, created.asString());
            assertEquals(title, created.jsonPath().getString("data.title"));
            assertEquals(data.courseId().toString(), created.jsonPath().getString("data.courseId"));
            assertEquals("LEARNING_MATERIAL", created.jsonPath().getString("data.purpose"));
            assertTrue(created.jsonPath().getLong("data.fileSize") > 0, created.asString());

            Response read = materialClient.get(materialId, teacher.accessToken());
            assertEquals(200, read.statusCode(), read.asString());
            assertEquals(materialId, read.jsonPath().getString("data.id"));
            assertEquals(title, read.jsonPath().getString("data.title"));

            Response listed = materialClient.list(Map.of(
                    "courseId", data.courseId().toString(), "search", title, "page", 0, "size", 20),
                    teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.content.id").contains(materialId),
                    listed.asString());

            Response downloaded = materialClient.download(materialId, teacher.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(materialId, downloaded.jsonPath().getString("data.id"));
            assertNotNull(downloaded.jsonPath().getString("data.downloadUrl"), downloaded.asString());
            assertTrue(downloaded.jsonPath().getString("data.storageKey") == null,
                    "download metadata must not expose storage credentials or keys");

            String updatedTitle = title + "-updated";
            Response updated = materialClient.update(materialId,
                    Map.of("title", updatedTitle, "description", "Updated TEST-013 material"),
                    teacher.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals(updatedTitle, updated.jsonPath().getString("data.title"));

            Response archived = materialClient.archive(materialId, teacher.accessToken());
            assertEquals(200, archived.statusCode(), archived.asString());
            assertEquals("ARCHIVED", archived.jsonPath().getString("data.status"));

            Response deleted = materialClient.delete(materialId, teacher.accessToken());
            assertEquals(200, deleted.statusCode(), deleted.asString());
            materialId = null;
        } finally {
            if (materialId != null) {
                materialClient.delete(materialId, teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void enrolledStudentCanUploadSubmissionAttachmentMaterial() {
        MaterialTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        String materialId = null;
        try (MaterialTestFileFactory.TestFile file = MaterialTestFileFactory.createTextFile()) {
            Map<String, Object> fields = materialFields(data,
                    "TEST-013-submission-" + java.util.UUID.randomUUID());
            fields.put("purpose", "SUBMISSION_ATTACHMENT");
            Response created = materialClient.upload(fields, file.path().toFile(), student.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            materialId = created.jsonPath().getString("data.id");
            assertEquals("SUBMISSION_ATTACHMENT", created.jsonPath().getString("data.purpose"));
        } finally {
            logout(student);
        }

        if (materialId != null) {
            AuthenticationSession teacher = login(TestRole.TEACHER);
            try {
                assertEquals(200, materialClient.delete(materialId, teacher.accessToken()).statusCode());
            } finally {
                logout(teacher);
            }
        }
    }

    @Test
    void rejectsInvalidMaterialUploadsAndUpdates() {
        MaterialTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try (MaterialTestFileFactory.TestFile empty = MaterialTestFileFactory.createEmptyFile();
             MaterialTestFileFactory.TestFile unsupported = MaterialTestFileFactory.createUnsupportedFile()) {
            assertEquals(400, materialClient.uploadWithoutFile(
                    Map.of("title", "missing-file", "courseId", data.courseId().toString()),
                    teacher.accessToken()).statusCode());
            assertEquals(400, materialClient.upload(
                    Map.of("courseId", data.courseId().toString()), empty.path().toFile(),
                    teacher.accessToken()).statusCode());
            assertEquals(400, materialClient.upload(
                    Map.of("title", "unsupported", "courseId", data.courseId().toString()),
                    unsupported.path().toFile(), teacher.accessToken()).statusCode());
            assertEquals(400, materialClient.update(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownMaterialOperationsAreNotReportedAsServerErrors() {
        requireReadFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertNotFoundOrBadRequest(materialClient.get(UNKNOWN_ID, teacher.accessToken()));
            assertNotFoundOrBadRequest(materialClient.download(UNKNOWN_ID, teacher.accessToken()));
            assertNotFoundOrBadRequest(materialClient.update(UNKNOWN_ID,
                    Map.of("title", "unknown"), teacher.accessToken()));
            assertNotFoundOrBadRequest(materialClient.archive(UNKNOWN_ID, teacher.accessToken()));
            assertNotFoundOrBadRequest(materialClient.delete(UNKNOWN_ID, teacher.accessToken()));
        } finally {
            logout(teacher);
        }
    }

    @Test
    void adminCanAuditAndRunNonDestructiveCleanup() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Map<String, Object> request = Map.of("dryRun", true);
            Response audit = materialClient.audit(request, admin.accessToken());
            assertEquals(200, audit.statusCode(), audit.asString());
            assertEquals(true, audit.jsonPath().getBoolean("data.dryRun"));

            Response cleanup = materialClient.cleanup(request, admin.accessToken());
            assertEquals(200, cleanup.statusCode(), cleanup.asString());
            assertEquals(true, cleanup.jsonPath().getBoolean("data.dryRun"));
        } finally {
            logout(admin);
        }
    }

    private static MaterialTestDataConfiguration requireReadFixtures() {
        MaterialTestDataConfiguration data = MaterialTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.readFixturesConfigured(),
                "Set TEST_MATERIAL_COURSE_ID and TEST_MATERIAL_ID");
        return data;
    }

    private static MaterialTestDataConfiguration requireWriteFixtures() {
        MaterialTestDataConfiguration data = MaterialTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.writeFixturesConfigured(),
                "Set TEST_MATERIAL_COURSE_ID and TEST_MATERIAL_WRITE_ENABLED=true");
        return data;
    }

    private static Map<String, Object> materialFields(MaterialTestDataConfiguration data, String title) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("title", title);
        fields.put("description", "Synthetic TEST-013 material");
        fields.put("purpose", "LEARNING_MATERIAL");
        fields.put("courseId", data.courseId().toString());
        return fields;
    }

    private static void assertNotFoundOrBadRequest(Response response) {
        assertTrue(response.statusCode() == 404 || response.statusCode() == 400,
                response.asString());
    }
}
