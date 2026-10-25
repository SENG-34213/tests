package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommunicationSettingsIT extends CommunicationIntegrationTestSupport {

    @Test
    void authenticatedUserCanReadAndUpdateOwnSettings() {
        AuthenticationSession student = login(TestRole.STUDENT);
        Map<String, Object> originalSettings = null;
        try {
            String token = student.accessToken();
            Response original = communicationClient.getSettings(token);
            assertEquals(200, original.statusCode(), original.asString());
            originalSettings = settingsFrom(original);
            assertEquals(200, communicationClient.updateSettings(CommunicationTestDataFactory.settings(true), token)
                    .statusCode());
            assertEquals(200, communicationClient.getSettings(token).statusCode());
        } finally {
            if (originalSettings != null) {
                communicationClient.updateSettings(originalSettings, student.accessToken());
            }
            logout(student);
        }
    }

    @Test
    void settingsChangesAreIsolatedToTheAuthenticatedUser() {
        assumeTrue(hasSecondaryStudentCredentials(),
                "Set distinct SECONDARY_STUDENT_TEST_USERNAME/PASSWORD credentials");
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession secondaryStudent = null;
        Map<String, Object> originalSettings = null;
        try {
            secondaryStudent = login(TestRole.SECONDARY_STUDENT);
            Response ownBefore = communicationClient.getSettings(student.accessToken());
            Response otherBefore = communicationClient.getSettings(secondaryStudent.accessToken());
            assertEquals(200, ownBefore.statusCode(), ownBefore.asString());
            assertEquals(200, otherBefore.statusCode(), otherBefore.asString());
            assertNotEqualUserSettings(ownBefore, otherBefore);
            originalSettings = settingsFrom(ownBefore);

            boolean changedReadReceipts = !ownBefore.jsonPath().getBoolean("data.readReceiptsEnabled");
            assertEquals(200, communicationClient.updateSettings(
                    Map.of("readReceiptsEnabled", changedReadReceipts), student.accessToken()).statusCode());

            Response ownAfter = communicationClient.getSettings(student.accessToken());
            Response otherAfter = communicationClient.getSettings(secondaryStudent.accessToken());
            assertEquals(changedReadReceipts, ownAfter.jsonPath().getBoolean("data.readReceiptsEnabled"));
            assertEquals(settingsFrom(otherBefore), settingsFrom(otherAfter), otherAfter.asString());
        } finally {
            if (originalSettings != null) {
                communicationClient.updateSettings(originalSettings, student.accessToken());
            }
            logout(secondaryStudent);
            logout(student);
        }
    }

    private static Map<String, Object> settingsFrom(Response response) {
        return Map.of(
                "readReceiptsEnabled", response.jsonPath().getBoolean("data.readReceiptsEnabled"),
                "lastSeenVisible", response.jsonPath().getBoolean("data.lastSeenVisible"),
                "profilePhotoVisible", response.jsonPath().getBoolean("data.profilePhotoVisible"),
                "groupNotificationsEnabled", response.jsonPath().getBoolean("data.groupNotificationsEnabled"),
                "directNotificationsEnabled", response.jsonPath().getBoolean("data.directNotificationsEnabled"));
    }

    private static void assertNotEqualUserSettings(Response first, Response second) {
        org.junit.jupiter.api.Assertions.assertNotEquals(
                first.jsonPath().getString("data.userId"), second.jsonPath().getString("data.userId"),
                "Isolation test requires distinct authenticated users");
    }

    private static boolean hasSecondaryStudentCredentials() {
        String username = System.getenv("SECONDARY_STUDENT_TEST_USERNAME");
        String password = System.getenv("SECONDARY_STUDENT_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
