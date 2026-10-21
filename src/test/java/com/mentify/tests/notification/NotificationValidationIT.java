package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationValidationIT extends NotificationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void unknownAndMalformedNotificationIdsReturnDocumentedClientErrors() {
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String token = user.accessToken();
            assertEquals(404, notifications.markRead(UNKNOWN_ID, token).statusCode());
            assertEquals(404, notifications.delete(UNKNOWN_ID, token).statusCode());
            assertEquals(400, notifications.markRead("not-a-uuid", token).statusCode());
        } finally {
            logout(user);
        }
    }

    @Test
    void httpEndpointRejectsInvalidPaginationRanges() {
        AuthenticationSession user = login(TestRole.STUDENT);
        try {
            String path = "/api/v1/notifications";
            String token = user.accessToken();
            Response response = apiClient.get(path, token, Map.of("page", -1), Map.of());
            assertEquals(400, response.statusCode(), response.asString());
            response = apiClient.get(path, token, Map.of("size", 0), Map.of());
            assertEquals(400, response.statusCode(), response.asString());
            response = apiClient.get(path, token, Map.of("size", 101), Map.of());
            assertEquals(400, response.statusCode(), response.asString());
        } finally {
            logout(user);
        }
    }
}
