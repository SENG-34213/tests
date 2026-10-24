package com.mentify.tests.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void expiredSignedAccessTokenIsRejected() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String expiredToken = ExpiredJwtFactory.expiredCopy(
                    student.accessToken(), System.getenv("AUTH_JWT_PRIVATE_KEY_BASE64"));

            assertEquals(401, users.currentUser(expiredToken).statusCode());
        } finally {
            logout(student);
        }
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
        assertEquals(401, response.statusCode());
        assertFalse(body.contains("password"));
        assertFalse(body.contains("access_token"));
        assertFalse(body.contains("refresh_token"));
        assertFalse(body.contains("bearer "));
        assertFalse(body.contains("stacktrace"));
    }
}
