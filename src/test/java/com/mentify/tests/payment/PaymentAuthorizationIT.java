package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PaymentAuthorizationIT extends PaymentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void unauthenticatedPaymentRequestsReturnUnauthorized() {
        assertEquals(401, payments.startCoursePayment(Map.of(), null).statusCode());
        assertEquals(401, payments.myPayments(null).statusCode());
        assertEquals(401, payments.myPayment(UNKNOWN_ID, null).statusCode());
        assertEquals(401, payments.adminPayments(null, null, null, null, null, null, 0, 20, null).statusCode());
        assertEquals(401, payments.verifySuccessfulPayment(UNKNOWN_ID, UNKNOWN_ID, null).statusCode());
    }

    @Test
    void studentCanUseOwnEndpointsButCannotUseAdministratorEndpoints() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            assertEquals(200, payments.myPayments(token).statusCode());
            assertEquals(403, payments.adminPayments(null, null, null, null, null, null, 0, 20, token).statusCode());
            assertEquals(403, payments.adminPayment(UNKNOWN_ID, token).statusCode());
            assertEquals(403, payments.refund(UNKNOWN_ID, null, token).statusCode());
            assertEquals(403, payments.verifySuccessfulPayment(UNKNOWN_ID, UNKNOWN_ID, token).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void administratorCanVerifySuccessfulPaymentForUnknownStudentCoursePair() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            var response = payments.verifySuccessfulPayment(UNKNOWN_ID, UNKNOWN_ID, admin.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertEquals(false, response.jsonPath().getBoolean("data.successfulPaymentExists"));
        } finally {
            logout(admin);
        }
    }

    @Test
    void teacherCannotUseStudentOrAdministratorPaymentEndpoints() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            String token = teacher.accessToken();
            assertEquals(403, payments.myPayments(token).statusCode());
            assertEquals(403, payments.adminPayments(null, null, null, null, null, null, 0, 20, token).statusCode());
            assertEquals(403, payments.refund(UNKNOWN_ID, null, token).statusCode());
        } finally {
            logout(teacher);
        }
    }
}
