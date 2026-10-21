package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class NotificationOwnershipIT extends NotificationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void twoAuthenticatedUsersReceiveOnlyTheirOwnNotificationCollections() {
        AuthenticationSession userA = login(TestRole.STUDENT);
        AuthenticationSession userB = login(TestRole.TEACHER);
        try {
            assertEquals(200, notifications.list(0, 20, false, userA.accessToken()).statusCode());
            assertEquals(200, notifications.list(0, 20, false, userB.accessToken()).statusCode());
            assertEquals(404, notifications.markRead(UNKNOWN_ID, userA.accessToken()).statusCode());
            assertEquals(404, notifications.delete(UNKNOWN_ID, userA.accessToken()).statusCode());
        } finally {
            logout(userA);
            logout(userB);
        }
    }

    @Test
    void anotherUserCannotMarkOrDeleteAStudentOwnedNotification() {
        NotificationTestData data = NotificationTestData.fromEnvironment();
        assumeTrue(data.notificationId() != null,
                "Set TEST_NOTIFICATION_ID to a notification owned by the configured student account");
        AuthenticationSession otherUser = login(TestRole.TEACHER);
        try {
            String notificationId = data.notificationId().toString();
            assertEquals(404, notifications.markRead(notificationId, otherUser.accessToken()).statusCode());
            assertEquals(404, notifications.delete(notificationId, otherUser.accessToken()).statusCode());
        } finally {
            logout(otherUser);
        }
    }
}
