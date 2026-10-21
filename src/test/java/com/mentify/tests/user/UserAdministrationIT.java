package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            assertRoleFilter(users.usersByRole("STUDENT", token), "data", "STUDENT");
            assertRoleFilter(users.usersByRole("TEACHER", token), "data", "TEACHER");
            assertRoleFilter(users.adminUsers("STUDENT", token), "data", "STUDENT");
            assertRoleFilter(users.listStudents(0, 20, token), "data.content", "STUDENT");
            assertRoleFilter(users.listTeachers(0, 20, token), "data.content", "TEACHER");
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
            assertEquals(400, users.updateStatus(UNKNOWN_ID, Map.of(), token).statusCode());
            assertEquals(404, users.updateStatus(UNKNOWN_ID, Map.of("active", true), token).statusCode());
            assertEquals(404, users.resendInvitation(UNKNOWN_ID, token).statusCode());
            assertEquals(404, users.deleteUser(UNKNOWN_ID, token).statusCode());
            assertEquals(400, users.registerUser(UserTestData.invalidRegistration(), token).statusCode());
            assertEquals(400, users.usersByRole("INVALID", token).statusCode());
        } finally {
            logout(admin);
        }
    }

    @Test
    void shouldRestrictUserAdministrationToAllowedRoles() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String studentToken = student.accessToken();
            assertEquals(403, users.usersByRole("STUDENT", studentToken).statusCode());
            assertEquals(403, users.adminUsers("STUDENT", studentToken).statusCode());
            assertEquals(403, users.listStudents(0, 10, studentToken).statusCode());
            assertEquals(403, users.listTeachers(0, 10, studentToken).statusCode());
            assertEquals(403, users.registerUser(UserTestData.invalidRegistration(), studentToken).statusCode());
            assertEquals(403, users.registerAdmin(Map.of(), admin.accessToken()).statusCode());
            assertEquals(403, users.deleteUser(UNKNOWN_ID, studentToken).statusCode());
            assertEquals(403, users.deleteUser(UNKNOWN_ID, teacher.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(teacher);
            logout(admin);
        }
    }

    private static void assertRoleFilter(io.restassured.response.Response response, String listPath, String role) {
        assertEquals(200, response.statusCode(), response.asString());
        java.util.List<String> roles = response.jsonPath().getList(listPath + ".role", String.class);
        assertFalse(roles.isEmpty(), "Expected at least one listed user for role " + role);
        assertTrue(roles.stream().allMatch(role::equals), response.asString());
    }
}
