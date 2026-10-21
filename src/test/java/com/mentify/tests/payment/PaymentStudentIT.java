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
            assertEquals(201, started.statusCode(), started.asString());
            String paymentId = started.jsonPath().getString("data.paymentId");
            assertTrue(paymentId != null && !paymentId.isBlank());
            assertEquals(data.courseId().toString(), started.jsonPath().getString("data.courseId"));
            assertTrue(started.jsonPath().getDouble("data.amount") > 0.0);
            assertEquals(3, started.jsonPath().getString("data.currency").length());
            assertEquals("PENDING", started.jsonPath().getString("data.status"));
            assertTrue(started.jsonPath().getString("data.clientSecret") != null
                    && !started.jsonPath().getString("data.clientSecret").isBlank());

            Response ownPayments = payments.myPayments(token);
            assertEquals(200, ownPayments.statusCode(), ownPayments.asString());
            List<Map<String, Object>> listedPayments = ownPayments.jsonPath().getList("data");
            assertTrue(listedPayments.stream().anyMatch(payment -> paymentId.equals(payment.get("paymentId"))));

            Response detail = payments.myPayment(paymentId, token);
            assertEquals(200, detail.statusCode(), detail.asString());
            assertEquals(paymentId, detail.jsonPath().getString("data.paymentId"));
            assertEquals(data.courseId().toString(), detail.jsonPath().getString("data.courseId"));
            assertEquals("PENDING", detail.jsonPath().getString("data.status"));
            assertEquals("STRIPE", detail.jsonPath().getString("data.provider"));

            Response status = payments.paymentStatus(paymentId, token);
            assertEquals(200, status.statusCode(), status.asString());
            assertEquals(paymentId, status.jsonPath().getString("data.paymentId"));
            assertEquals(detail.jsonPath().getString("data.status"), status.jsonPath().getString("data.status"));

            Response verified = payments.verifySuccessfulPayment(data.studentId().toString(),
                    data.courseId().toString(), token);
            assertEquals(200, verified.statusCode(), verified.asString());
            assertEquals(data.studentId().toString(), verified.jsonPath().getString("data.studentId"));
            assertEquals(data.courseId().toString(), verified.jsonPath().getString("data.courseId"));

            if (data.secondStudentId() != null && secondaryStudentCredentialsConfigured()) {
                AuthenticationSession otherStudent = login(TestRole.SECONDARY_STUDENT);
                try {
                    List<Map<String, Object>> otherPayments = payments.myPayments(otherStudent.accessToken())
                            .jsonPath().getList("data");
                    assertTrue(otherPayments.stream().noneMatch(payment -> paymentId.equals(payment.get("paymentId"))));
                    assertEquals(404, payments.myPayment(paymentId, otherStudent.accessToken()).statusCode());
                    assertEquals(404, payments.paymentStatus(paymentId, otherStudent.accessToken()).statusCode());
                } finally {
                    logout(otherStudent);
                }
            }
        } finally {
            logout(student);
        }
    }

    private static boolean secondaryStudentCredentialsConfigured() {
        String username = System.getenv("SECONDARY_STUDENT_TEST_USERNAME");
        String password = System.getenv("SECONDARY_STUDENT_TEST_PASSWORD");
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }
}
