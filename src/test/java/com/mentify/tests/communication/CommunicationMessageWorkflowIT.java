package com.mentify.tests.communication;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class CommunicationMessageWorkflowIT extends CommunicationIntegrationTestSupport {

    @Test
    void shouldUploadAttachDownloadAndCleanUpMessageAttachment() throws Exception {
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.groupId() != null && data.writeEnabled(),
                "Set TEST_COMMUNICATION_GROUP_ID and TEST_COMMUNICATION_WRITE_ENABLED=true; "
                        + "the teacher must be an active member of the group");
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(RequiredService.MATERIAL_SERVICE);

        AuthenticationSession teacher = login(TestRole.TEACHER);
        MaterialClient materials = new MaterialClient(apiClient);
        String groupId = data.groupId().toString();
        String materialId = null;
        String messageId = null;
        String attachmentId = null;
        Path file = Files.createTempFile("mentify-communication-attachment-", ".txt");
        String content = "Synthetic message attachment " + UUID.randomUUID();
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
            Response uploaded = communicationClient.uploadMessageAttachment(groupId, file.toFile(),
                    teacher.accessToken());
            materialId = uploaded.jsonPath().getString("data.materialId");
            assertEquals(201, uploaded.statusCode(), uploaded.asString());
            assertNotNull(materialId, uploaded.asString());
            assertTrue(uploaded.jsonPath().getLong("data.fileSize") > 0, uploaded.asString());

            Response sent = communicationClient.sendMessage(groupId,
                    Map.of("content", "Synthetic attachment message", "attachmentIds", List.of(materialId)),
                    teacher.accessToken());
            messageId = sent.jsonPath().getString("data.id");
            assertEquals(201, sent.statusCode(), sent.asString());
            assertNotNull(messageId, sent.asString());
            assertEquals(materialId, sent.jsonPath().getString("data.attachments[0].materialId"));
            attachmentId = sent.jsonPath().getString("data.attachments[0].id");
            assertNotNull(attachmentId, sent.asString());

            Response downloaded = communicationClient.downloadMessageAttachment(groupId, messageId,
                    attachmentId, teacher.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertEquals(file.getFileName().toString(), downloaded.jsonPath().getString("data.originalFilename"));
            assertTrue(downloaded.jsonPath().getLong("data.fileSize") > 0, downloaded.asString());
            assertNotNull(downloaded.jsonPath().getString("data.downloadUrl"), downloaded.asString());

            assertEquals(200, communicationClient.deleteMessage(groupId, messageId,
                    "EVERYONE", teacher.accessToken()).statusCode());
            messageId = null;
        } finally {
            if (messageId != null) {
                communicationClient.deleteMessage(groupId, messageId, "EVERYONE", teacher.accessToken());
            }
            if (materialId != null) {
                materials.delete(materialId, teacher.accessToken());
            }
            Files.deleteIfExists(file);
            logout(teacher);
        }
    }

    @Test
    void teacherCanSendPageUpdateAndDeleteMessages() {
        CommunicationTestDataConfiguration data = CommunicationTestDataConfiguration.fromEnvironment();
        Assumptions.assumeTrue(data.groupId() != null && data.writeEnabled(),
                "Communication write fixtures are not enabled");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            Response sent = communicationClient.sendMessage(data.groupId().toString(),
                    CommunicationTestDataFactory.message("TEST-016 " + System.nanoTime()), token);
            assertEquals(201, sent.statusCode(), sent.asString());
            String messageId = sent.jsonPath().getString("data.id");
            assertNotNull(messageId, sent.asString());
            Response history = communicationClient.messageHistory(data.groupId().toString(), 0, 101, token);
            assertEquals(200, history.statusCode(), history.asString());
            assertEquals(100, history.jsonPath().getInt("data.size"), history.asString());
            try {
                Response updated = communicationClient.updateMessage(data.groupId().toString(), messageId,
                        CommunicationTestDataFactory.message("updated TEST-016"), token);
                assertEquals(200, updated.statusCode(), updated.asString());
                assertEquals("updated TEST-016", updated.jsonPath().getString("data.content"));
            } finally {
                assertEquals(200, communicationClient.deleteMessage(data.groupId().toString(), messageId,
                        "EVERYONE", token).statusCode());
            }
        } finally {
            logout(teacher);
        }
    }
}
