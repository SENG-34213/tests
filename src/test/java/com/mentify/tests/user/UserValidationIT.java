package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
            var malformedStudentId = users.studentExists("not-a-valid-id", token);
            assertEquals(200, malformedStudentId.statusCode());
            assertFalse(malformedStudentId.as(Boolean.class));
            var unknownStudentId = users.studentExists(UNKNOWN_ID, token);
            assertEquals(200, unknownStudentId.statusCode());
            assertFalse(unknownStudentId.as(Boolean.class));
            var unknownTeacherId = users.teacherExists(UNKNOWN_ID, token);
            assertEquals(200, unknownTeacherId.statusCode());
            assertFalse(unknownTeacherId.as(Boolean.class));
            assertThrows(IllegalArgumentException.class, () -> users.teacherExists("", token));
            assertEquals(400, users.updateStatus(UNKNOWN_ID, Map.of("active", "invalid"), token).statusCode());
        } finally {
            logout(admin);
        }
    }
}
