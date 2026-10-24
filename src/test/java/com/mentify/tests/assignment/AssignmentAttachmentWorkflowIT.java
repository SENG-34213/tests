package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.enrollment.EnrollmentClient;
import com.mentify.tests.material.MaterialDownloadVerifier;
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
    void rejectsUnenrolledSecondaryStudentFromAssignmentAttachments() {
        AssignmentAttachmentTestDataConfiguration data = AssignmentAttachmentTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.assignmentId() != null,
                "Set TEST_ATTACHMENT_ASSIGNMENT_ID for assignment attachment ownership checks");
        Assumptions.assumeTrue(secondaryStudentCredentialsConfigured(),
                "Configure distinct SECONDARY_STUDENT_TEST_USERNAME and SECONDARY_STUDENT_TEST_PASSWORD");

        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession otherStudent = null;
        try {
            Response assignment = assignmentClient.get(data.assignmentId().toString(), teacher.accessToken());
            assertEquals(200, assignment.statusCode(), assignment.asString());
            String courseId = assignment.jsonPath().getString("data.courseId");
            assertNotNull(courseId, assignment.asString());

            Response listed = assignmentAttachmentClient.list(data.assignmentId().toString(), teacher.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            java.util.List<String> attachmentIds = listed.jsonPath().getList("data.id", String.class);
            Assumptions.assumeTrue(attachmentIds != null && !attachmentIds.isEmpty(),
                    "Configure an existing attachment on TEST_ATTACHMENT_ASSIGNMENT_ID");

            otherStudent = login(TestRole.SECONDARY_STUDENT);
            Response profile = apiClient.get("/api/v1/users/me", otherStudent.accessToken(), Map.of(), Map.of());
            assertEquals(200, profile.statusCode(), profile.asString());
            String otherStudentId = profile.jsonPath().getString("data.id");
            assertNotNull(otherStudentId, profile.asString());

            Response membership = new EnrollmentClient(apiClient).membership(
                    otherStudentId, courseId, otherStudent.accessToken());
            assertEquals(200, membership.statusCode(), membership.asString());
            Assumptions.assumeFalse(membership.as(Boolean.class),
                    "Secondary student must not be enrolled in the assignment course");

            assertEquals(403, assignmentAttachmentClient.list(
                    data.assignmentId().toString(), otherStudent.accessToken()).statusCode());
            assertEquals(403, assignmentAttachmentClient.download(
                    data.assignmentId().toString(), attachmentIds.get(0), otherStudent.accessToken()).statusCode());
        } finally {
            logout(otherStudent);
            logout(teacher);
        }
    }

    @Test
    void teacherCanUploadAndCleanUpSyntheticAssignmentAttachment() throws Exception {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        Path file = Files.createTempFile("mentify-test-assignment-", ".txt");
        String content = "Synthetic assignment attachment " + java.util.UUID.randomUUID();
        try (CleanupScope cleanup = new CleanupScope()) {
            cleanup.add(() -> Files.deleteIfExists(file));
            AuthenticationSession teacher = login(TestRole.TEACHER);
            cleanup.add(() -> logout(teacher));

            Files.writeString(file, content);
            Response created = assignmentAttachmentClient.upload(
                    data.assignmentId().toString(), file.toFile(), teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            String attachmentId = created.jsonPath().getString("data.id");
            String materialId = created.jsonPath().getString("data.materialId");
            if (materialId != null) {
                cleanup.add(() -> assertEquals(200,
                        materialClient.delete(materialId, teacher.accessToken()).statusCode()));
            }
            if (attachmentId != null) {
                cleanup.add(() -> assertEquals(200, assignmentAttachmentClient.delete(
                        data.assignmentId().toString(), attachmentId, teacher.accessToken()).statusCode()));
            }
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
                "Set TEST_ATTACHMENT_WRITE_ENABLED=true for attachment write tests");
        return data;
    }

    private static Map<String, String> materialRequest(java.util.UUID materialId) {
        return Map.of("materialId", materialId.toString());
    }

    private static boolean secondaryStudentCredentialsConfigured() {
        String username = System.getenv("SECONDARY_STUDENT_TEST_USERNAME");
        String password = System.getenv("SECONDARY_STUDENT_TEST_PASSWORD");
        String primaryUsername = System.getenv("STUDENT_TEST_USERNAME");
        if (primaryUsername == null || primaryUsername.isBlank()) {
            primaryUsername = System.getenv("TEST_USERNAME");
        }
        return username != null && !username.isBlank() && password != null && !password.isBlank()
                && !username.equalsIgnoreCase(primaryUsername == null ? "" : primaryUsername.trim());
    }
}
