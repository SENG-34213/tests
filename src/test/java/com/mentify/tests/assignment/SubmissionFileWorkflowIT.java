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

class SubmissionFileWorkflowIT extends AssignmentIntegrationTestSupport {

    @Test
    void studentCanCreateListDownloadAndDeleteSubmissionMaterialFile() {
        AssignmentAttachmentTestDataConfiguration data = requireWriteFixtures();
        AuthenticationSession student = login(TestRole.STUDENT);
        String fileId = null;
        try {
            Response created = submissionFileClient.create(data.submissionId().toString(),
                    Map.of("materialId", data.materialId().toString()), student.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            assertEquals("success", created.jsonPath().getString("status"));
            fileId = created.jsonPath().getString("data.id");
            assertNotNull(fileId, created.asString());
            assertEquals(data.materialId().toString(), created.jsonPath().getString("data.materialId"));

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
            assertTrue(response.statusCode() == 404 || response.statusCode() == 400,
                    response.asString());
        } finally {
            logout(student);
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
}
