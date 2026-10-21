package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationIT extends UserIntegrationTestSupport {

    @Test
    void publicAndProtectedAuthenticationProbesFollowTheirSecurityRules() {
        assertEquals(200, users.publicTest().statusCode());
        assertEquals(401, users.protectedTest(null).statusCode());
        assertEquals(401, users.adminTest(null).statusCode());
        assertEquals(401, users.studentTest(null).statusCode());
    }

    @Test
    void roleProbesMatchStudentAndAdministratorTokens() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            assertEquals(200, users.protectedTest(student.accessToken()).statusCode());
            assertEquals(200, users.studentTest(student.accessToken()).statusCode());
            assertEquals(403, users.adminTest(student.accessToken()).statusCode());
            assertEquals(200, users.adminTest(admin.accessToken()).statusCode());
            assertEquals(403, users.studentTest(admin.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(admin);
        }
    }

    @Test
    void invalidAuthenticationPayloadsAreRejectedWithoutSensitiveOutput() {
        assertEquals(400, users.login(Map.of()).statusCode());
        assertEquals(401, users.login(Map.of("identifier", "invalid", "password", "invalid")).statusCode());
        assertEquals(400, users.refresh(Map.of()).statusCode());
        assertEquals(400, users.logout(Map.of()).statusCode());
        assertEquals(400, users.forgotPassword(Map.of("email", "invalid")).statusCode());
    }
}
