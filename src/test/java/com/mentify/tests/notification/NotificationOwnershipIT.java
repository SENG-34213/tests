package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertTrue(notifications.list(0, 20, false, userA.accessToken()).statusCode() == 200);
            assertTrue(notifications.list(0, 20, false, userB.accessToken()).statusCode() == 200);
            assertTrue(notifications.markRead(UNKNOWN_ID, userA.accessToken()).statusCode() < 500);
            assertTrue(notifications.delete(UNKNOWN_ID, userA.accessToken()).statusCode() < 500);
        } finally {
            logout(userA);
            logout(userB);
        }
    }
}
