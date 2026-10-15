package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class PaymentAdminIT extends PaymentIntegrationTestSupport {

    @Test
    void administratorCanListFilterAndInspectPayments() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            PaymentTestData data = PaymentTestData.fromEnvironment();
            String token = admin.accessToken();
            Response listed = payments.adminPayments(null, null, null, null, null, null, 0, 20, token);
            assertEquals(200, listed.statusCode());
            assertTrue(listed.jsonPath().get("data") != null);
            if (data.fixturesConfigured()) {
                assertEquals(200, payments.adminPayments(null, data.studentId().toString(),
                        data.courseId().toString(), null, null, null, 0, 20, token).statusCode());
                assertEquals(200, payments.adminPayments(null, null, data.courseId().toString(), null, null,
                        null, 0, 1, token).statusCode());
            }
        } finally {
            logout(admin);
        }
    }

    @Test
    void administratorCanInspectAndSafelyRefundAnApprovedTestPayment() {
        PaymentTestData data = PaymentTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_PAYMENT_WRITE_ENABLED=true and approved provider fixtures to run refund tests");
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            Response listed = payments.adminPayments(null, data.studentId().toString(), data.courseId().toString(),
                    null, null, null, 0, 20, token);
            assertEquals(200, listed.statusCode());
            String paymentId = listed.jsonPath().getString("data.content[0].paymentId");
            assumeTrue(paymentId != null, "No approved payment fixture is available for refund testing");
            assertEquals(200, payments.adminPayment(paymentId, token).statusCode());
            assertTrue(payments.refund(paymentId, data.refundRequest(), token).statusCode() < 500);
        } finally {
            logout(admin);
        }
    }
}
