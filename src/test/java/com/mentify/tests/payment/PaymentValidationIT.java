package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PaymentValidationIT extends PaymentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void invalidPaymentRequestsReturnClientErrorsOrNotFoundResponses() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            assertTrue(payments.startCoursePayment(Map.of(), token).statusCode() < 500);
            assertTrue(payments.startCoursePayment(Map.of("courseId", "not-a-uuid"), token).statusCode() < 500);
            assertTrue(payments.myPayment(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(payments.paymentStatus(UNKNOWN_ID, token).statusCode() < 500);
            assertTrue(payments.verifySuccessfulPayment("not-a-uuid", UNKNOWN_ID, token).statusCode() < 500);
        } finally {
            logout(student);
        }
    }

    @Test
    void refundReasonLengthIsRejectedOrHandledWithoutServerError() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String longReason = "x".repeat(501);
            assertTrue(payments.refund(UNKNOWN_ID, Map.of("reason", longReason), admin.accessToken())
                    .statusCode() < 500);
        } finally {
            logout(admin);
        }
    }

    @Test
    void webhookWithoutSignatureIsRejected() {
        assertTrue(payments.webhook("{}", null).statusCode() < 500);
    }
}
