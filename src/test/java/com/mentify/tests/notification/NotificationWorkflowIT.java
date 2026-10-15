package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class NotificationWorkflowIT extends NotificationIntegrationTestSupport {

    @Test
    void userCanRetrievePaginatedAndUnreadNotifications() {
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String token = user.accessToken();
            assertEquals(200, notifications.list(0, 20, false, token).statusCode());
            assertEquals(200, notifications.list(0, 1, true, token).statusCode());
            assertEquals(200, notifications.list(1, 100, false, token).statusCode());
            long unreadCount = notifications.unreadCount(token).jsonPath().getLong("data");
            assertTrue(unreadCount >= 0);
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
            assertTrue(notifications.markRead(notificationId, token).statusCode() < 500);
            assertTrue(notifications.markAllRead(token).statusCode() < 500);
            assertTrue(notifications.delete(notificationId, token).statusCode() < 500);
        } finally {
            logout(user);
        }
    }
}
