package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiConversationIT extends AiIntegrationTestSupport {

    @Test
    void differentAdminOwnerCannotReadOrMutateTeachersConversation() {
        AiTestData data = AiTestData.fromEnvironment();
        assumeTrue(data.writeEnabled(), "Set TEST_AI_WRITE_ENABLED=true for conversation ownership tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession admin = null;
        String conversationId = null;
        try {
            admin = login(TestRole.ADMIN);
            Response teacherInfo = ai.me(teacher.accessToken());
            Response adminInfo = ai.me(admin.accessToken());
            assertEquals(200, teacherInfo.statusCode(), teacherInfo.asString());
            assertEquals(200, adminInfo.statusCode(), adminInfo.asString());
            String teacherId = teacherInfo.jsonPath().getString("userId");
            String adminId = adminInfo.jsonPath().getString("userId");
            assumeTrue(teacherId != null && adminId != null && !teacherId.equals(adminId),
                    "Configure distinct teacher and admin credentials for ownership checks");

            Response created = ai.createConversation(Map.of("title", "TEST-020 ownership " + System.nanoTime()),
                    teacher.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            conversationId = created.jsonPath().getString("data.id");
            assertTrue(conversationId != null && !conversationId.isBlank(), created.asString());

            assertEquals(404, ai.conversation(conversationId, admin.accessToken()).statusCode());
            assertEquals(404, ai.messages(conversationId, admin.accessToken()).statusCode());
            assertEquals(404, ai.updateConversation(conversationId, Map.of("title", "unauthorized"),
                    admin.accessToken()).statusCode());
            assertEquals(404, ai.deleteConversation(conversationId, admin.accessToken()).statusCode());
        } finally {
            try {
                if (conversationId != null) {
                    ai.deleteConversation(conversationId, teacher.accessToken());
                }
            } finally {
                logout(admin);
                logout(teacher);
            }
        }
    }

    @Test
    void teacherCanUseTheConversationLifecycle() {
        AiTestData data = AiTestData.fromEnvironment();
        assumeTrue(data.writeEnabled(), "Set TEST_AI_WRITE_ENABLED=true for conversation workflow tests");
        AuthenticationSession teacher = login(TestRole.TEACHER);
        String conversationId = null;
        try {
            String token = teacher.accessToken();
            String title = "TEST-020 " + System.nanoTime();
            Response created = ai.createConversation(Map.of("title", title), token);
            assertEquals(201, created.statusCode(), created.asString());
            conversationId = created.jsonPath().getString("data.id");
            assertTrue(conversationId != null && !conversationId.isBlank());
            Response listed = ai.conversations(token);
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.id", String.class).contains(conversationId),
                    listed.asString());

            Response read = ai.conversation(conversationId, token);
            assertEquals(200, read.statusCode(), read.asString());
            assertEquals(conversationId, read.jsonPath().getString("data.id"));
            assertEquals(title, read.jsonPath().getString("data.title"));

            Response messages = ai.messages(conversationId, token);
            assertEquals(200, messages.statusCode(), messages.asString());
            assertNotNull(messages.jsonPath().get("data"), messages.asString());

            String updatedTitle = "Updated TEST-020 " + System.nanoTime();
            Response updated = ai.updateConversation(conversationId, Map.of("title", updatedTitle), token);
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals(updatedTitle, updated.jsonPath().getString("data.title"));

            Response deleted = ai.deleteConversation(conversationId, token);
            assertEquals(200, deleted.statusCode(), deleted.asString());
            conversationId = null;
            assertEquals(404, ai.conversation(read.jsonPath().getString("data.id"), token).statusCode());
        } finally {
            try {
                if (conversationId != null) {
                    ai.deleteConversation(conversationId, teacher.accessToken());
                }
            } finally {
                logout(teacher);
            }
        }
    }
}
