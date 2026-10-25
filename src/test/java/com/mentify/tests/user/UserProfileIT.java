package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class UserProfileIT extends UserIntegrationTestSupport {

    @Test
    void requiredUserServicesAreReady() {
        requireUserEnvironment();
    }

    @Test
    void authenticatedUserCanRetrieveOwnProfileWithoutSensitiveFields() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            var response = users.currentUser(student.accessToken());
            assertEquals(200, response.statusCode());
            String body = response.asString().toLowerCase();
            assertFalse(body.contains("password"));
            assertFalse(body.contains("refresh_token"));
            assertFalse(body.contains("client_secret"));
        } finally {
            logout(student);
        }
    }
}
