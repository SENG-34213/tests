package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AssignmentAttachmentWorkflowIT extends AssignmentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void teacherCanCreateListDownloadAndDeleteAssignmentMaterialAttachment() {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String attachmentId = null;
        try {
            Response created = assignmentAttachmentClient.create(data.assignmentId().toString(),
                    materialRequest(data.materialId()), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals("success", created.jsonPath().getString("status"));
            attachmentId = created.jsonPath().getString("data.id");
            assertNotNull(attachmentId, created.asString());
            assertEquals(data.materialId().toString(), created.jsonPath().getString("data.materialId"));

            Response listed = assignmentAttachmentClient.list(data.assignmentId().toString(),
                    teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(attachmentId), listed.asString());

            Response downloaded = assignmentAttachmentClient.download(data.assignmentId().toString(),
                    attachmentId, teacher.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(attachmentId, downloaded.jsonPath().getString("data.id"));
            assertEquals(data.materialId().toString(), downloaded.jsonPath().getString("data.materialId"));

            AuthenticationSession student = login(TestRole.STUDENT);
            try {
                Response studentList = assignmentAttachmentClient.list(data.assignmentId().toString(),
                        student.accessToken());
                assertEquals(200, studentList.statusCode(), studentList.asString());
                assertTrue(studentList.jsonPath().getList("data.id").contains(attachmentId),
                        studentList.asString());
                Response studentDownload = assignmentAttachmentClient.download(
                        data.assignmentId().toString(), attachmentId, student.accessToken());
                assertEquals(200, studentDownload.statusCode(), studentDownload.asString());
            } finally {
                logout(student);
            }

            Response deleted = assignmentAttachmentClient.delete(data.assignmentId().toString(),
                    attachmentId, teacher.accessToken());
            assertEquals(200, deleted.statusCode(), deleted.asString());
            attachmentId = null;
        } finally {
            if (attachmentId != null) {
                assignmentAttachmentClient.delete(data.assignmentId().toString(), attachmentId,
                        teacher.accessToken());
            }
            logout(teacher);
        }
    }

    @Test
    void rejectsUnknownAssignmentAttachmentResources() {
        AssignmentAttachmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            Response response = assignmentAttachmentClient.list(UNKNOWN_ID, teacher.accessToken());
            assertEquals(404, response.statusCode(), response.asString());

            response = assignmentAttachmentClient.download(data.assignmentId().toString(), UNKNOWN_ID,
                    teacher.accessToken());
            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void teacherCanUploadAndCleanUpSyntheticAssignmentAttachment() throws Exception {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        Path file = Files.createTempFile("mentify-test-assignment-", ".txt");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String content = "Synthetic assignment attachment " + java.util.UUID.randomUUID();
        String attachmentId = null;
        String materialId = null;
        try {
            Files.writeString(file, content);
            Response created = assignmentAttachmentClient.upload(
                    data.assignmentId().toString(), file.toFile(), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            attachmentId = created.jsonPath().getString("data.id");
            materialId = created.jsonPath().getString("data.materialId");
            assertNotNull(attachmentId, created.asString());
            assertNotNull(materialId, created.asString());
            assertEquals(content.length(), created.jsonPath().getLong("data.fileSize"));
            assertEquals(file.getFileName().toString(), created.jsonPath().getString("data.originalFilename"));

            Response listed = assignmentAttachmentClient.list(data.assignmentId().toString(), teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(attachmentId), listed.asString());

            Response downloaded = assignmentAttachmentClient.download(
                    data.assignmentId().toString(), attachmentId, teacher.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(materialId, downloaded.jsonPath().getString("data.materialId"));
            assertNotNull(downloaded.jsonPath().getString("data.downloadUrl"), downloaded.asString());
        } finally {
            try {
                if (attachmentId != null) {
                    assertEquals(200, assignmentAttachmentClient.delete(data.assignmentId().toString(), attachmentId,
                            teacher.accessToken()).statusCode());
                }
            } finally {
                try {
                    if (materialId != null) {
                        assertEquals(200, materialClient.delete(materialId, teacher.accessToken()).statusCode());
                    }
                } finally {
                    logout(teacher);
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    private static AssignmentAttachmentTestDataConfiguration requireReadFixtures() {
        AssignmentAttachmentTestDataConfiguration data =
                AssignmentAttachmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.readFixturesConfigured(),
                "Set TEST_ATTACHMENT_ASSIGNMENT_ID, TEST_ATTACHMENT_SUBMISSION_ID and "
                        + "TEST_ATTACHMENT_MATERIAL_ID");
        return data;
    }

    private static AssignmentAttachmentTestDataConfiguration requireWriteFixtures() {
        AssignmentAttachmentTestDataConfiguration data = requireReadFixtures();
        Assumptions.assumeTrue(data.writeFixturesConfigured(),
                "Set TEST_ATTACHMENT_WRITE_ENABLED=true for attachment write tests");
        return data;
    }

    private static Map<String, String> materialRequest(java.util.UUID materialId) {
        return Map.of("materialId", materialId.toString());
    }
}
