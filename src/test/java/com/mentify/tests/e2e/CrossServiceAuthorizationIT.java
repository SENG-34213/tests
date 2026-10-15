package com.mentify.tests.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import org.junit.jupiter.api.Test;

class CrossServiceAuthorizationIT extends EndToEndIntegrationTestSupport {

    @Test
    void gatewayPreservesRoleRestrictionsAcrossUserAndEnrollmentServices() {
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
}
