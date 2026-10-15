package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

class PaymentStudentIT extends PaymentIntegrationTestSupport {

    @Test
    void studentCanStartAndReadOwnCoursePayment() {
        PaymentTestData data = PaymentTestData.fromEnvironment();
        assumeTrue(data.writeEnabled() && data.fixturesConfigured(),
                "Set TEST_PAYMENT_WRITE_ENABLED=true and approved payment fixtures to run provider tests");
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            Response started = payments.startCoursePayment(data.startRequest(), token);
            assertTrue(started.statusCode() == 201 || started.statusCode() == 200);
            String paymentId = started.jsonPath().getString("data.paymentId");
            assertTrue(paymentId != null && !paymentId.isBlank());
            assertEquals(200, payments.myPayments(token).statusCode());
            assertEquals(200, payments.myPayment(paymentId, token).statusCode());
            assertEquals(200, payments.paymentStatus(paymentId, token).statusCode());
            assertEquals(200, payments.verifySuccessfulPayment(data.studentId().toString(),
                    data.courseId().toString(), token).statusCode());
        } finally {
            logout(student);
        }
    }
}
