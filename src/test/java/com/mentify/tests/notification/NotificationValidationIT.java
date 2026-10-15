package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class NotificationValidationIT extends NotificationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void unknownAndMalformedNotificationIdsNeverProduceServerErrors() {
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String token = user.accessToken();
            assertTrue(notifications.markRead(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(notifications.delete(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(notifications.markRead("not-a-uuid", token).statusCode() < 500);
        } finally {
            logout(user);
        }
    }
}
