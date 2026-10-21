package com.mentify.tests.payment;

import com.mentify.tests.payment.model.RefundPaymentRequest;
import com.mentify.tests.payment.model.StartCoursePaymentRequest;
import java.util.UUID;

record PaymentTestData(UUID courseId, UUID studentId, UUID secondStudentId, UUID refundPaymentId,
                       UUID webhookSuccessPaymentId, UUID webhookFailurePaymentId,
                       boolean writeEnabled, boolean webhookWriteEnabled) {

    static PaymentTestData fromEnvironment() {
        return new PaymentTestData(uuid("TEST_PAYMENT_COURSE_ID"), uuid("TEST_PAYMENT_STUDENT_ID"),
                uuid("TEST_PAYMENT_SECOND_STUDENT_ID"), uuid("TEST_PAYMENT_REFUND_ID"),
                uuid("TEST_PAYMENT_WEBHOOK_SUCCESS_ID"), uuid("TEST_PAYMENT_WEBHOOK_FAILURE_ID"), Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_PAYMENT_WRITE_ENABLED", "false")), Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_PAYMENT_WEBHOOK_WRITE_ENABLED", "false")));
    }

    boolean fixturesConfigured() {
        return courseId != null && studentId != null;
    }

    boolean refundFixtureConfigured() {
        return fixturesConfigured() && refundPaymentId != null && writeEnabled;
    }

    boolean webhookSuccessFixtureConfigured() {
        return webhookSuccessPaymentId != null && webhookWriteEnabled;
    }

    boolean webhookFailureFixtureConfigured() {
        return webhookFailurePaymentId != null && webhookWriteEnabled;
    }

    StartCoursePaymentRequest startRequest() {
        return new StartCoursePaymentRequest(courseId);
    }

    RefundPaymentRequest refundRequest() {
        return new RefundPaymentRequest("TEST-018 safe test refund");
    }

    private static UUID uuid(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid UUID for " + name, exception);
        }
    }
}
