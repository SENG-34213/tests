package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GatewayContractTest extends ContractTestSupport {

    @Test
    void gatewayPreservesProtectedEndpointAuthenticationContract() {
        var response = users.protectedTest(null);
        assertEquals(401, response.statusCode());
        ContractAssertions.assertErrorResponse(response);
    }

    @Test
    void gatewayPreservesPublicEndpointContract() {
        var response = users.publicTest();
        assertEquals(200, response.statusCode());
        ContractAssertions.assertCommonResponse(response);
    }
}
