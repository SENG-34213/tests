package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommonResponseContractTest extends ContractTestSupport {

    @Test
    void publicGatewayResponsePreservesCommonContract() {
        var response = users.publicTest();
        assertEquals(200, response.statusCode());
        ContractAssertions.assertCommonResponse(response);
    }

    @Test
    void unauthenticatedResponsePreservesCommonErrorContract() {
        var response = users.protectedTest(null);
        assertEquals(401, response.statusCode());
        ContractAssertions.assertErrorResponse(response);
    }
}
