package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
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
            assertNotFoundOrBadRequest(response);

            response = assignmentAttachmentClient.download(data.assignmentId().toString(), UNKNOWN_ID,
                    teacher.accessToken());
            assertNotFoundOrBadRequest(response);
        } finally {
            logout(teacher);
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

    private static void assertNotFoundOrBadRequest(Response response) {
        assertTrue(response.statusCode() == 404 || response.statusCode() == 400,
                response.asString());
    }
}
