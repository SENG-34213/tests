package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            assertEquals(400, communicationClient.createGroup(Map.of(), teacher.accessToken()).statusCode());
            assertEquals(400, communicationClient.sendMessage(UNKNOWN_ID, Map.of(), teacher.accessToken()).statusCode());
            assertEquals(400, communicationClient.updateSettings(
                    Map.of("readReceiptsEnabled", "not-a-boolean"), teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void unknownCommunicationIdsNeverProduceServerErrors() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(404, communicationClient.getGroup(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, communicationClient.listMembers(UNKNOWN_ID, teacher.accessToken()).statusCode());
            assertEquals(404, communicationClient.messageHistory(UNKNOWN_ID, 0, 10,
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
