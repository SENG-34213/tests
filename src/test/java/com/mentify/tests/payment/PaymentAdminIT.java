package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PaymentAdminIT extends PaymentIntegrationTestSupport {

    @Test
    void administratorCanListFilterAndInspectPayments() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            PaymentTestData data = PaymentTestData.fromEnvironment();
            String token = admin.accessToken();
            Response listed = payments.adminPayments(null, null, null, null, null, null, 0, 20, token);
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().get("data") != null);
            assertPageContract(listed, 20, 0);
            if (data.fixturesConfigured()) {
                Response ownerAndCourse = payments.adminPayments(null, data.studentId().toString(),
                        data.courseId().toString(), null, null, null, 0, 20, token);
                assertEquals(200, ownerAndCourse.statusCode(), ownerAndCourse.asString());
                List<Map<String, Object>> matchingPayments = ownerAndCourse.jsonPath().getList("data.content");
                assertTrue(matchingPayments.stream().allMatch(payment ->
                        data.studentId().toString().equals(payment.get("studentId"))
                                && data.courseId().toString().equals(payment.get("courseId"))));
                assertPageContract(ownerAndCourse, 20, 0);

                Response successful = payments.adminPayments("SUCCESS", data.studentId().toString(),
                        data.courseId().toString(), "2000-01-01T00:00:00", "2100-01-01T00:00:00",
                        null, 0, 20, token);
                assertEquals(200, successful.statusCode(), successful.asString());
                List<Map<String, Object>> successfulPayments = successful.jsonPath().getList("data.content");
                assertTrue(successfulPayments.stream().allMatch(payment -> "SUCCESS".equals(payment.get("status"))));
                assertPageContract(successful, 20, 0);

                if (!matchingPayments.isEmpty()) {
                    String paymentId = String.valueOf(matchingPayments.get(0).get("paymentId"));
                    Response byReference = payments.adminPayments(null, null, null, null, null,
                            paymentId, 0, 1, token);
                    assertEquals(200, byReference.statusCode(), byReference.asString());
                    assertEquals(paymentId, byReference.jsonPath().getString("data.content[0].paymentId"));
                    assertPageContract(byReference, 1, 0);
                }
            }
        } finally {
            logout(admin);
        }
    }

    @Test
    void administratorCanInspectAndSafelyRefundAnApprovedTestPayment() {
        PaymentTestData data = PaymentTestData.fromEnvironment();
        assumeTrue(data.refundFixtureConfigured(),
                "Set TEST_PAYMENT_WRITE_ENABLED=true and TEST_PAYMENT_REFUND_ID to a dedicated successful test payment");
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            String paymentId = data.refundPaymentId().toString();
            Response beforeRefund = payments.adminPayment(paymentId, token);
            assertEquals(200, beforeRefund.statusCode(), beforeRefund.asString());
            assertEquals(data.studentId().toString(), beforeRefund.jsonPath().getString("data.studentId"));
            assertEquals(data.courseId().toString(), beforeRefund.jsonPath().getString("data.courseId"));
            assertEquals("SUCCESS", beforeRefund.jsonPath().getString("data.status"));
            String paymentIntentId = beforeRefund.jsonPath().getString("data.stripePaymentIntentId");
            assertTrue(paymentIntentId != null && !paymentIntentId.isBlank());

            Response refunded = payments.refund(paymentId, data.refundRequest(), token);
            assertEquals(200, refunded.statusCode(), refunded.asString());
            assertEquals(paymentId, refunded.jsonPath().getString("data.paymentId"));
            assertEquals("REFUNDED", refunded.jsonPath().getString("data.status"));
            assertTrue(refunded.jsonPath().getString("data.stripeRefundId") != null);
            assertTrue(refunded.jsonPath().getString("data.refundedAt") != null);
            Response afterRefund = payments.adminPayment(paymentId, token);
            assertEquals("REFUNDED", afterRefund.jsonPath().getString("data.status"));
            assertEquals(data.refundRequest().reason(), afterRefund.jsonPath().getString("data.refundReason"));
        } finally {
            logout(admin);
        }
    }

    private static void assertPageContract(Response response, int expectedSize, int expectedPage) {
        List<Map<String, Object>> content = response.jsonPath().getList("data.content");
        assertEquals(expectedPage, response.jsonPath().getInt("data.number"));
        assertEquals(expectedSize, response.jsonPath().getInt("data.size"));
        assertTrue(content.size() <= expectedSize);
        assertTrue(response.jsonPath().getLong("data.totalElements") >= content.size());
    }
}
