package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserContractTest extends ContractTestSupport {

    @Test
    void currentUserResponsePreservesStableFields() {
        AuthenticationSession session = login(TestRole.STUDENT);
        try {
            var response = users.currentUser(session.accessToken());
            assertEquals(200, response.statusCode());
            ContractAssertions.assertCommonResponse(response);
        } finally {
            logout(session);
        }
    }

    @Test
    void invalidLoginPreservesErrorContract() {
        var response = users.login(Map.of("identifier", "invalid@example.test", "password", "invalid"));
        ContractAssertions.assertErrorResponse(response);
    }
}
