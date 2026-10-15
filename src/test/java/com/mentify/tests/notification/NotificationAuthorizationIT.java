package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class NotificationAuthorizationIT extends NotificationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void unauthenticatedNotificationRequestsReturnUnauthorized() {
        assertEquals(401, notifications.list(0, 20, false, null).statusCode());
        assertEquals(401, notifications.unreadCount(null).statusCode());
        assertEquals(401, notifications.markRead(UNKNOWN_ID, null).statusCode());
        assertEquals(401, notifications.markAllRead(null).statusCode());
        assertEquals(401, notifications.delete(UNKNOWN_ID, null).statusCode());
    }

    @Test
    void notificationAccessIsAvailableToAuthenticatedUsersOnly() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(200, notifications.list(0, 20, false, student.accessToken()).statusCode());
            assertEquals(200, notifications.unreadCount(student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}
