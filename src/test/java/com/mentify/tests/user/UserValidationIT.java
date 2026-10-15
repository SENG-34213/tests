package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserValidationIT extends UserIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void invalidUserIdentifiersAndStatusPayloadsAreHandledSafely() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            assertTrue(users.studentExists("not-a-valid-id", token).statusCode() < 500);
            assertThrows(IllegalArgumentException.class, () -> users.teacherExists("", token));
            assertTrue(users.updateStatus(UNKNOWN_ID, Map.of("active", "invalid"), token).statusCode() < 500);
            assertTrue(users.listStudents(-1, 0, token).statusCode() < 500);
            assertTrue(users.listTeachers(0, 1000, token).statusCode() < 500);
        } finally {
            logout(admin);
        }
    }
}
