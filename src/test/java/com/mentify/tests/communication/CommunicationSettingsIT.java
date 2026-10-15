package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class CommunicationSettingsIT extends CommunicationIntegrationTestSupport {

    @Test
    void authenticatedUserCanReadAndUpdateOwnSettings() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            assertEquals(200, communicationClient.getSettings(token).statusCode());
            assertEquals(200, communicationClient.updateSettings(CommunicationTestDataFactory.settings(true), token)
                    .statusCode());
            assertEquals(200, communicationClient.getSettings(token).statusCode());
        } finally {
            logout(student);
        }
    }
}
