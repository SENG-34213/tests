package com.mentify.tests.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthenticationResponseParserTest {

    @Test
    void shouldParseTheBackendAuthenticationEnvelope() {
        Response response = response(200, """
                {"statusCode":200,"message":"Login successful","data":{
                "accessToken":"access-secret","refreshToken":"refresh-secret",
                "tokenType":"Bearer","expiresIn":300,"refreshExpiresIn":1800,
                "scope":"openid email profile","issuedAt":"2026-01-01T00:00:00Z"}}
                """);

        AuthenticationSession session = AuthenticationResponseParser.successfulSession(response);

        assertEquals("access-secret", session.accessToken());
        assertEquals("refresh-secret", session.refreshToken());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), session.issuedAt());
    }

    @Test
    void shouldExposeOnlySafeErrorDetails() {
        Response response = response(401,
                "{\"statusCode\":401,\"message\":\"Invalid credentials\","
                        + "\"debugToken\":\"secret\"}");

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> AuthenticationResponseParser.successfulSession(response));

        assertEquals(401, exception.statusCode());
        assertEquals("Invalid credentials", exception.getMessage());
    }

    private static Response response(int status, String body) {
        return new ResponseBuilder().setStatusCode(status).setBody(body).build();
    }
}
