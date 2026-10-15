package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommunicationValidationIT extends CommunicationIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void invalidRequestsReturnClientErrors() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertTrue(communicationClient.createGroup(Map.of(), teacher.accessToken()).statusCode() < 500);
            assertTrue(communicationClient.sendMessage(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode() < 500);
            assertTrue(communicationClient.updateSettings(Map.of("unknown", true), teacher.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownCommunicationIdsNeverProduceServerErrors() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertTrue(communicationClient.getGroup(UNKNOWN_ID, teacher.accessToken()).statusCode() < 500);
            assertTrue(communicationClient.listMembers(UNKNOWN_ID, teacher.accessToken()).statusCode() < 500);
            assertTrue(communicationClient.messageHistory(UNKNOWN_ID, 0, 10, teacher.accessToken()).statusCode() < 500);
        } finally {
            logout(teacher);
        }
    }
}
