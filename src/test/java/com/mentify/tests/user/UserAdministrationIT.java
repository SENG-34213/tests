package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserAdministrationIT extends UserIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void administratorCanListUsersAndReadAnalytics() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            assertEquals(200, users.usersByRole("STUDENT", token).statusCode());
            assertEquals(200, users.usersByRole("TEACHER", token).statusCode());
            assertEquals(200, users.adminUsers("STUDENT", token).statusCode());
            assertEquals(200, users.listStudents(0, 20, token).statusCode());
            assertEquals(200, users.listTeachers(0, 20, token).statusCode());
            assertEquals(200, users.analytics(token).statusCode());
        } finally {
            logout(admin);
        }
    }

    @Test
    void administratorOperationsValidateUnknownUsersAndFields() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            assertTrue(users.updateStatus(UNKNOWN_ID, Map.of(), token).statusCode() < 500);
            assertTrue(users.resendInvitation(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(users.registerUser(UserTestData.invalidRegistration(), token).statusCode() < 500);
            assertTrue(users.usersByRole("INVALID", token).statusCode() < 500);
        } finally {
            logout(admin);
        }
    }
}
