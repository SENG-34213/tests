package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.material.MaterialDownloadVerifier;
import io.restassured.response.Response;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class SubmissionFileWorkflowIT extends AssignmentIntegrationTestSupport {

    @Test
    void anotherStudentCannotReadOrDeleteSubmissionFiles() throws Exception {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        assumeTrue(secondaryStudentCredentialsConfigured(),
                "Set distinct SECONDARY_STUDENT_TEST_USERNAME and SECONDARY_STUDENT_TEST_PASSWORD");

        Path file = Files.createTempFile("mentify-test-submission-owner-", ".txt");
        AuthenticationSession owner = login(TestRole.STUDENT);
        AuthenticationSession otherStudent = null;
        String fileId = null;
        String materialId = null;
        try {
            Files.writeString(file, "Submission ownership probe " + java.util.UUID.randomUUID());
            Response uploaded = submissionFileClient.upload(
                    data.submissionId().toString(), file.toFile(), owner.accessToken());
            assertEquals(201, uploaded.statusCode(), uploaded.asString());
            fileId = uploaded.jsonPath().getString("data.id");
            materialId = uploaded.jsonPath().getString("data.materialId");
            assertNotNull(fileId, uploaded.asString());
            assertNotNull(materialId, uploaded.asString());

            otherStudent = login(TestRole.SECONDARY_STUDENT);
            assertEquals(403, submissionFileClient.list(data.submissionId().toString(),
                    otherStudent.accessToken()).statusCode());
            assertEquals(403, submissionFileClient.download(data.submissionId().toString(), fileId,
                    otherStudent.accessToken()).statusCode());
            assertEquals(403, submissionFileClient.delete(data.submissionId().toString(), fileId,
                    otherStudent.accessToken()).statusCode());
        } finally {
            try {
                if (fileId != null) {
                    assertEquals(200, submissionFileClient.delete(data.submissionId().toString(), fileId,
                            owner.accessToken()).statusCode());
                }
            } finally {
                try {
                    logout(otherStudent);
                    logout(owner);
                    if (materialId != null) {
                        AuthenticationSession admin = login(TestRole.ADMIN);
                        try {
                            assertEquals(200, materialClient.delete(materialId, admin.accessToken()).statusCode());
                        } finally {
                            logout(admin);
                        }
                    }
                } finally {
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    @Test
    void studentCanCreateListDownloadAndDeleteSubmissionMaterialFile() {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        assumeTrue(data.submissionMaterialId() != null,
                "Set TEST_ATTACHMENT_SUBMISSION_MATERIAL_ID to a SUBMISSION_ATTACHMENT material");
        AuthenticationSession student = login(TestRole.STUDENT);
        String fileId = null;
        try {
            Response created = submissionFileClient.create(data.submissionId().toString(),
                    Map.of("materialId", data.submissionMaterialId().toString()), student.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            fileId = created.jsonPath().getString("data.id");
            assertNotNull(fileId, created.asString());
            assertEquals(data.submissionMaterialId().toString(), created.jsonPath().getString("data.materialId"));

            Response listed = submissionFileClient.list(data.submissionId().toString(),
                    student.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(fileId), listed.asString());

            Response downloaded = submissionFileClient.download(data.submissionId().toString(), fileId,
                    student.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(fileId, downloaded.jsonPath().getString("data.id"));

            AuthenticationSession teacher = login(TestRole.TEACHER);
            try {
                Response teacherList = submissionFileClient.list(data.submissionId().toString(),
                        teacher.accessToken());
                assertEquals(200, teacherList.statusCode(), teacherList.asString());
                assertTrue(teacherList.jsonPath().getList("data.id").contains(fileId),
                        teacherList.asString());
                Response teacherDownload = submissionFileClient.download(data.submissionId().toString(),
                        fileId, teacher.accessToken());
                assertEquals(200, teacherDownload.statusCode(), teacherDownload.asString());
                assertEquals(fileId, teacherDownload.jsonPath().getString("data.id"));
            } finally {
                logout(teacher);
            }

            Response deleted = submissionFileClient.delete(data.submissionId().toString(), fileId,
                    student.accessToken());
            assertEquals(200, deleted.statusCode(), deleted.asString());
            fileId = null;
        } finally {
            if (fileId != null) {
                submissionFileClient.delete(data.submissionId().toString(), fileId,
                        student.accessToken());
            }
            logout(student);
        }
    }

    @Test
    void rejectsUnknownSubmissionFileResources() {
        AssignmentAttachmentTestDataConfiguration data = requireReadFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            Response response = submissionFileClient.list(data.submissionId().toString(),
                    student.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            response = submissionFileClient.download(data.submissionId().toString(),
                    "00000000-0000-0000-0000-000000000000", student.accessToken());
            assertEquals(404, response.statusCode(), response.asString());
        } finally {
            logout(student);
        }
    }

    @Test
    void studentCanUploadAndCleanUpSyntheticSubmissionFile() throws Exception {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        Path file = Files.createTempFile("mentify-test-submission-", ".txt");
        String content = "Synthetic submission file " + java.util.UUID.randomUUID();
        try (CleanupScope cleanup = new CleanupScope()) {
            cleanup.add(() -> Files.deleteIfExists(file));
            AuthenticationSession student = login(TestRole.STUDENT);
            cleanup.add(() -> logout(student));

            Files.writeString(file, content);
            Response created = submissionFileClient.upload(
                    data.submissionId().toString(), file.toFile(), student.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            String fileId = created.jsonPath().getString("data.id");
            String materialId = created.jsonPath().getString("data.materialId");
            if (materialId != null) {
                cleanup.add(() -> {
                    AuthenticationSession admin = login(TestRole.ADMIN);
                    try {
                        assertEquals(200, materialClient.delete(materialId, admin.accessToken()).statusCode());
                    } finally {
                        logout(admin);
                    }
                });
            }
            if (fileId != null) {
                cleanup.add(() -> assertEquals(200,
                        submissionFileClient.delete(data.submissionId().toString(), fileId,
                                student.accessToken()).statusCode()));
            }
            assertNotNull(fileId, created.asString());
            assertNotNull(materialId, created.asString());
            assertEquals(content.length(), created.jsonPath().getLong("data.fileSize"));
            assertEquals(file.getFileName().toString(), created.jsonPath().getString("data.originalFilename"));

            Response listed = submissionFileClient.list(data.submissionId().toString(), student.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id").contains(fileId), listed.asString());

            Response downloaded = submissionFileClient.download(
                    data.submissionId().toString(), fileId, student.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(materialId, downloaded.jsonPath().getString("data.materialId"));
            assertNotNull(downloaded.jsonPath().getString("data.downloadUrl"), downloaded.asString());
            MaterialDownloadVerifier.verifyIfEnabled(
                    downloaded.jsonPath().getString("data.downloadUrl"), file);
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
                "Set TEST_ATTACHMENT_WRITE_ENABLED=true for file write tests");
        return data;
    }

    private static boolean secondaryStudentCredentialsConfigured() {
        String username = System.getenv("SECONDARY_STUDENT_TEST_USERNAME");
        String password = System.getenv("SECONDARY_STUDENT_TEST_PASSWORD");
        String primaryUsername = System.getenv("STUDENT_TEST_USERNAME");
        if (primaryUsername == null || primaryUsername.isBlank()) {
            primaryUsername = System.getenv("TEST_USERNAME");
        }
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && !username.equalsIgnoreCase(primaryUsername == null ? "" : primaryUsername.trim());
    }
}
