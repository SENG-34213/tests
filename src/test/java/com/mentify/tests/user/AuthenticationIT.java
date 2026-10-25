package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationIT extends UserIntegrationTestSupport {

    @Test
    void publicAndProtectedAuthenticationProbesFollowTheirSecurityRules() {
        var publicProbe = users.publicTest();
        assertEquals(200, publicProbe.statusCode(), publicProbe.asString());
        assertEquals("success", publicProbe.jsonPath().getString("status"));
        assertTrue(publicProbe.jsonPath().getString("message").contains("Public endpoint"));
        assertEquals(400, users.login(Map.of("identifier", "unknown-user@example.invalid", "password", "invalid"))
                .statusCode());
        assertEquals(401, users.currentUser(null).statusCode());
        assertEquals(401, users.adminUsers("STUDENT", null).statusCode());
    }

    @Test
    void studentProbeRequiresTheStudentRole() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(401, users.studentTest(null).statusCode());
            var studentResponse = users.studentTest(student.accessToken());
            assertEquals(200, studentResponse.statusCode(), studentResponse.asString());
            assertEquals("success", studentResponse.jsonPath().getString("status"));
            assertTrue(studentResponse.jsonPath().getString("message").contains("Student authorization"));
            assertEquals(403, users.studentTest(teacher.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(teacher);
        }
    }

    @Test
    void roleProbesMatchStudentAndAdministratorTokens() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            assertEquals(200, users.currentUser(student.accessToken()).statusCode());
            assertEquals(403, users.adminUsers("STUDENT", student.accessToken()).statusCode());
            assertEquals(200, users.adminUsers("STUDENT", admin.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(admin);
        }
    }

    @Test
    void invalidAuthenticationPayloadsAreRejectedWithoutSensitiveOutput() {
        assertEquals(400, users.login(Map.of()).statusCode());
        assertEquals(400, users.login(Map.of("identifier", "invalid", "password", "invalid")).statusCode());
        assertEquals(400, users.refresh(Map.of()).statusCode());
        assertEquals(400, users.logout(Map.of()).statusCode());
        assertEquals(400, users.forgotPassword(Map.of("email", "invalid")).statusCode());
    }
}
