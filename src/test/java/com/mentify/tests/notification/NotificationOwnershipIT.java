package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class NotificationOwnershipIT extends NotificationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void twoAuthenticatedUsersReceiveOnlyTheirOwnNotificationCollections() {
        NotificationTestData data = NotificationTestData.fromEnvironment();
        assumeTrue(data.notificationId() != null,
                "Set TEST_NOTIFICATION_ID to a notification owned by the configured student account");
        AuthenticationSession userA = login(TestRole.STUDENT);
        AuthenticationSession userB = login(TestRole.TEACHER);
        try {
            var studentNotifications = notifications.list(0, 100, false, userA.accessToken());
            var teacherNotifications = notifications.list(0, 100, false, userB.accessToken());
            assertEquals(200, studentNotifications.statusCode(), studentNotifications.asString());
            assertEquals(200, teacherNotifications.statusCode(), teacherNotifications.asString());
            String studentOwnedId = data.notificationId().toString();
            assertTrue(studentNotifications.jsonPath().getList("data.notifications.id", String.class)
                    .contains(studentOwnedId), "The configured student fixture must appear in its owner's list");
            assertFalse(teacherNotifications.jsonPath().getList("data.notifications.id", String.class)
                    .contains(studentOwnedId), "A different user must not receive the student's notification");
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
