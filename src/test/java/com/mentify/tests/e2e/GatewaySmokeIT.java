package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class GatewaySmokeIT extends EndToEndIntegrationTestSupport {

    @Test
    void authenticatedUserTravelsThroughGatewayToUserService() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            var response = users.currentUser(student.accessToken());
            assertEquals(200, response.statusCode());
            assertEquals(200, users.protectedTest(student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void missingGatewayTokenIsRejected() {
        assertEquals(401, users.protectedTest(null).statusCode());
    }
}
