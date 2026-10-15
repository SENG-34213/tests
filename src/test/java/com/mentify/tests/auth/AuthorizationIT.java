package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class AuthorizationIT extends AuthenticationIntegrationTestSupport {

    @Test
    void shouldAllowPublicAccessWithoutAuthentication() {
        Response response = apiClient.get("/api/v1/auth/public-test");

        assertEquals(200, response.statusCode());
        assertEquals("success", response.jsonPath().getString("status"));
    }

    @Test
    void shouldRejectProtectedAccessWithoutAuthentication() {
        Response response = apiClient.get("/api/v1/auth/protected-test");

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldAllowAStudentToUseStudentResources() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = authenticated(session).get("/api/v1/auth/student-test");

            assertEquals(200, response.statusCode());
            assertEquals("success", response.jsonPath().getString("status"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldRejectAStudentFromAdminResources() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            assertEquals(403, authenticated(session).get("/api/v1/auth/admin-test").statusCode());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowATeacherToUseAuthenticatedResourcesButNotAdminResources() {
        AuthenticationSession session = login(TestRole.TEACHER);

        try {
            assertEquals(200, authenticated(session).get("/api/v1/auth/protected-test").statusCode());
            assertEquals(403, authenticated(session).get("/api/v1/auth/admin-test").statusCode());
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowAnAdminToUseAdminResources() {
        AuthenticationSession session = login(TestRole.ADMIN);

        try {
            Response response = authenticated(session).get("/api/v1/auth/admin-test");

            assertEquals(200, response.statusCode());
            assertTrue(response.jsonPath().getString("message").contains("Admin authorization"));
        } finally {
            logout(session);
        }
    }

    @Test
    void shouldAllowAnAuthenticatedUserToReadTheirProfile() {
        AuthenticationSession session = login(TestRole.STUDENT);

        try {
            Response response = authenticated(session).get("/api/v1/users/me");

            assertEquals(200, response.statusCode());
            assertEquals(200, response.jsonPath().getInt("statusCode"));
            assertTrue(response.jsonPath().get("data") != null);
        } finally {
            logout(session);
        }
    }

    private static ApiClient authenticated(AuthenticationSession session) {
        return authenticationClient.authenticatedApiClient(session);
    }
}
