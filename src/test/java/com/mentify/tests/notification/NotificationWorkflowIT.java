package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationWorkflowIT extends NotificationIntegrationTestSupport {

    @Test
    void userCanRetrievePaginatedAndUnreadNotifications() {
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String token = user.accessToken();
            Response defaults = notifications.list(token);
            assertEquals(200, defaults.statusCode(), defaults.asString());
            assertEquals(0, defaults.jsonPath().getInt("data.page"));
            assertEquals(20, defaults.jsonPath().getInt("data.size"));
            assertPageContract(defaults, 20);

            Response minimumPageSize = notifications.list(0, 1, false, token);
            assertEquals(200, minimumPageSize.statusCode(), minimumPageSize.asString());
            assertPageContract(minimumPageSize, 1);

            Response unread = notifications.list(0, 100, true, token);
            assertEquals(200, unread.statusCode(), unread.asString());
            assertPageContract(unread, 100);
            List<Map<String, Object>> unreadNotifications = unread.jsonPath().getList("data.notifications");
            assertTrue(unreadNotifications.stream().allMatch(notification -> notification.get("readAt") == null),
                    "Unread-only results must contain no read notifications");

            Response laterPage = notifications.list(1, 100, false, token);
            assertEquals(200, laterPage.statusCode(), laterPage.asString());
            assertEquals(1, laterPage.jsonPath().getInt("data.page"));
            assertPageContract(laterPage, 100);
            long unreadCount = notifications.unreadCount(token).jsonPath().getLong("data");
            assertTrue(unreadCount >= 0);
            assertTrue(unreadCount >= unreadNotifications.size());
            assertTrue(unread.jsonPath().getLong("data.unreadCount") >= unreadNotifications.size());
        } finally {
            logout(user);
        }
    }

    @Test
    void ownerCanMarkReadMarkAllReadAndDeleteApprovedFixture() {
        NotificationTestData data = NotificationTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.notificationId() != null,
                "Set TEST_NOTIFICATION_WRITE_ENABLED=true and an approved notification ID");
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String token = user.accessToken();
            String notificationId = data.notificationId().toString();
            Response marked = notifications.markRead(notificationId, token);
            assertEquals(200, marked.statusCode(), marked.asString());
            assertEquals(notificationId, marked.jsonPath().getString("data.id"));
            String firstReadAt = marked.jsonPath().getString("data.readAt");
            assertTrue(firstReadAt != null && !firstReadAt.isBlank());

            Response markedAgain = notifications.markRead(notificationId, token);
            assertEquals(200, markedAgain.statusCode(), markedAgain.asString());
            assertEquals(firstReadAt, markedAgain.jsonPath().getString("data.readAt"),
                    "Mark-read must be idempotent and preserve its original read timestamp");

            Response allRead = notifications.markAllRead(token);
            assertEquals(200, allRead.statusCode(), allRead.asString());
            assertTrue(allRead.jsonPath().getInt("data") >= 0);

            Response deleted = notifications.delete(notificationId, token);
            assertEquals(200, deleted.statusCode(), deleted.asString());
        } finally {
            logout(user);
        }
    }

    private static void assertPageContract(Response response, int requestedSize) {
        List<Map<String, Object>> notifications = response.jsonPath().getList("data.notifications");
        assertTrue(notifications.size() <= requestedSize);
        assertEquals(requestedSize, response.jsonPath().getInt("data.size"));
        assertTrue(response.jsonPath().getLong("data.totalElements") >= notifications.size());
        assertTrue(response.jsonPath().getInt("data.totalPages") >= 0);
        assertTrue(response.jsonPath().getLong("data.unreadCount") >= 0);
    }
}
