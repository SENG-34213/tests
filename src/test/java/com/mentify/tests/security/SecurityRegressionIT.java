package com.mentify.tests.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class SecurityRegressionIT extends SecurityRegressionTestSupport {

    @Test
    void protectedEndpointsRejectMissingInvalidAndMalformedTokens() {
        assertEquals(401, users.currentUser(null).statusCode());
        assertEquals(401, users.currentUser("").statusCode());
        assertEquals(401, users.currentUser("not-a-jwt").statusCode());
        assertEquals(401, users.currentUser("header.payload.signature").statusCode());
        assertEquals(401, users.protectedTest("not-a-jwt").statusCode());
    }

    @Test
    void studentAndTeacherTokensCannotCrossAdministratorBoundary() {
        AuthenticationSession student = login(TestRole.STUDENT);
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, users.adminTest(student.accessToken()).statusCode());
            assertEquals(403, users.adminTest(teacher.accessToken()).statusCode());
        } finally {
            logout(student);
            logout(teacher);
        }
    }

    @Test
    void protectedErrorResponsesDoNotExposeSensitiveValues() {
        Response response = users.currentUser("not-a-jwt");
        String body = response.asString().toLowerCase();
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500);
        assertFalse(body.contains("password"));
        assertFalse(body.contains("access_token"));
        assertFalse(body.contains("refresh_token"));
        assertFalse(body.contains("bearer "));
        assertFalse(body.contains("stacktrace"));
    }
}
