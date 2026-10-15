package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommunicationAuthenticationIT extends CommunicationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void protectedCommunicationRequestsRejectMissingAuthentication() {
        assertEquals(401, communicationClient.me(null).statusCode());
        assertEquals(401, communicationClient.listGroups(null).statusCode());
        assertEquals(401, communicationClient.getGroup(UNKNOWN_ID, null).statusCode());
        assertEquals(401, communicationClient.listMembers(UNKNOWN_ID, null).statusCode());
        assertEquals(401, communicationClient.sendMessage(UNKNOWN_ID, Map.of("content", "blocked"), null).statusCode());
        assertEquals(401, communicationClient.getSettings(null).statusCode());
    }

    @Test
    void studentCannotManageGroups() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, communicationClient.createGroup(Map.of(), student.accessToken()).statusCode());
            assertEquals(403, communicationClient.addStudent(UNKNOWN_ID, Map.of(), student.accessToken()).statusCode());
            assertEquals(403, communicationClient.archive(UNKNOWN_ID, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }
}
