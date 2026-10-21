package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class PaymentValidationIT extends PaymentIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";

    @Test
    void invalidPaymentRequestsReturnExactClientErrorsOrNotFoundResponses() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String token = student.accessToken();
            assertEquals(400, payments.startCoursePayment(Map.of(), token).statusCode());
            assertEquals(400, payments.startCoursePayment(Map.of("courseId", "not-a-uuid"), token).statusCode());
            assertEquals(404, payments.startCoursePayment(Map.of("courseId", UNKNOWN_ID), token).statusCode());
            assertEquals(404, payments.myPayment(UNKNOWN_ID, token).statusCode());
            assertEquals(404, payments.paymentStatus(UNKNOWN_ID, token).statusCode());
            assertEquals(400, payments.verifySuccessfulPayment("not-a-uuid", UNKNOWN_ID, token).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void refundReasonOverLimitReturnsBadRequest() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String longReason = "x".repeat(501);
            assertEquals(400, payments.refund(UNKNOWN_ID, Map.of("reason", longReason), admin.accessToken())
                    .statusCode());
        } finally {
            logout(admin);
        }
    }

    @Test
    void invalidAdminFilterValuesReturnBadRequest() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String token = admin.accessToken();
            assertEquals(400, payments.adminPayments(null, null, null,
                    "2030-02-02T00:00:00", "2030-02-01T00:00:00", null, 0, 20, token).statusCode());
            assertEquals(400, payments.adminPayments("NOT_A_PAYMENT_STATUS", null, null,
                    null, null, null, 0, 20, token).statusCode());
            assertEquals(400, payments.adminPayments(null, "not-a-uuid", null,
                    null, null, null, 0, 20, token).statusCode());
            assertEquals(404, payments.adminPayment(UNKNOWN_ID, token).statusCode());
            assertEquals(404, payments.refund(UNKNOWN_ID, null, token).statusCode());
        } finally {
            logout(admin);
        }
    }

    @Test
    void webhookWithoutSignatureIsRejected() {
        assertEquals(400, payments.webhook("{}", null).statusCode());
    }

    @Test
    void webhookWithInvalidSignatureIsRejected() {
        assertEquals(400, payments.webhook("{}", "not-a-valid-stripe-signature").statusCode());
    }

    @Test
    void signedUnsupportedWebhookIsAcceptedAndSafeToReplay() {
        String secret = System.getenv("TEST_STRIPE_WEBHOOK_SECRET");
        Assumptions.assumeTrue(Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_STRIPE_WEBHOOK_TEST_ENABLED", "false"))
                        && secret != null && !secret.isBlank(),
                "Set TEST_STRIPE_WEBHOOK_TEST_ENABLED=true and the test Payment Service webhook secret");

        long created = Instant.now().getEpochSecond();
        String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "");
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");
        String payload = """
                {"id":"%s","object":"event","api_version":"2026-09-30.endive",
                "created":%d,"type":"payment_intent.created","data":{"object":{
                "id":"%s","object":"payment_intent","metadata":{}}}}
                """.formatted(eventId, created, paymentIntentId);
        String signature = signatureHeader(payload, secret, created);

        assertEquals(200, payments.webhook(payload, signature).statusCode());
        assertEquals(200, payments.webhook(payload, signature).statusCode());
    }

    @Test
    void signedSuccessWebhookTransitionsDedicatedPendingPaymentAndIsIdempotent() {
        PaymentTestData data = PaymentTestData.fromEnvironment();
        String secret = System.getenv("TEST_STRIPE_WEBHOOK_SECRET");
        Assumptions.assumeTrue(data.webhookSuccessFixtureConfigured()
                        && Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_STRIPE_WEBHOOK_TEST_ENABLED", "false"))
                        && secret != null && !secret.isBlank(),
                "Set TEST_PAYMENT_WEBHOOK_WRITE_ENABLED=true, TEST_PAYMENT_WEBHOOK_SUCCESS_ID, "
                        + "TEST_STRIPE_WEBHOOK_TEST_ENABLED=true, and TEST_STRIPE_WEBHOOK_SECRET for a "
                        + "dedicated disposable pending PaymentIntent");

        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String paymentId = data.webhookSuccessPaymentId().toString();
            var before = payments.adminPayment(paymentId, admin.accessToken());
            assertEquals(200, before.statusCode(), before.asString());
            Assumptions.assumeTrue("PENDING".equals(before.jsonPath().getString("data.status")),
                    "The one-time webhook fixture must still be PENDING; replenish it before rerunning");
            String paymentIntentId = before.jsonPath().getString("data.stripePaymentIntentId");
            assertTrue(paymentIntentId != null && !paymentIntentId.isBlank(), before.asString());

            long created = Instant.now().getEpochSecond();
            String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "");
            String payload = """
                    {"id":"%s","object":"event","api_version":"2026-09-30.endive",
                    "created":%d,"type":"payment_intent.succeeded","data":{"object":{
                    "id":"%s","object":"payment_intent","metadata":{"payment_id":"%s"}}}}
                    """.formatted(eventId, created, paymentIntentId, paymentId);
            String signature = signatureHeader(payload, secret, created);

            assertEquals(200, payments.webhook(payload, signature).statusCode());
            assertEquals(200, payments.webhook(payload, signature).statusCode());

            var after = payments.adminPayment(paymentId, admin.accessToken());
            assertEquals(200, after.statusCode(), after.asString());
            assertEquals("SUCCESS", after.jsonPath().getString("data.status"));
            assertEquals(paymentIntentId, after.jsonPath().getString("data.stripePaymentIntentId"));
            assertTrue(after.jsonPath().getString("data.paidAt") != null, after.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void signedFailureWebhookTransitionsDedicatedPendingPaymentAndIsIdempotent() {
        PaymentTestData data = PaymentTestData.fromEnvironment();
        String secret = System.getenv("TEST_STRIPE_WEBHOOK_SECRET");
        Assumptions.assumeTrue(data.webhookFailureFixtureConfigured()
                        && Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_STRIPE_WEBHOOK_TEST_ENABLED", "false"))
                        && secret != null && !secret.isBlank(),
                "Set TEST_PAYMENT_WEBHOOK_WRITE_ENABLED=true, TEST_PAYMENT_WEBHOOK_FAILURE_ID, "
                        + "TEST_STRIPE_WEBHOOK_TEST_ENABLED=true, and TEST_STRIPE_WEBHOOK_SECRET for a "
                        + "dedicated disposable pending PaymentIntent");

        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            String paymentId = data.webhookFailurePaymentId().toString();
            var before = payments.adminPayment(paymentId, admin.accessToken());
            assertEquals(200, before.statusCode(), before.asString());
            Assumptions.assumeTrue("PENDING".equals(before.jsonPath().getString("data.status")),
                    "The one-time webhook fixture must still be PENDING; replenish it before rerunning");
            String paymentIntentId = before.jsonPath().getString("data.stripePaymentIntentId");
            assertTrue(paymentIntentId != null && !paymentIntentId.isBlank(), before.asString());

            long created = Instant.now().getEpochSecond();
            String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "");
            String payload = """
                    {"id":"%s","object":"event","api_version":"2026-09-30.endive",
                    "created":%d,"type":"payment_intent.payment_failed","data":{"object":{
                    "id":"%s","object":"payment_intent","metadata":{"payment_id":"%s"}}}}
                    """.formatted(eventId, created, paymentIntentId, paymentId);
            String signature = signatureHeader(payload, secret, created);

            assertEquals(200, payments.webhook(payload, signature).statusCode());
            assertEquals(200, payments.webhook(payload, signature).statusCode());

            var after = payments.adminPayment(paymentId, admin.accessToken());
            assertEquals(200, after.statusCode(), after.asString());
            assertEquals("FAILED", after.jsonPath().getString("data.status"));
            assertEquals(paymentIntentId, after.jsonPath().getString("data.stripePaymentIntentId"));
            assertNull(after.jsonPath().getString("data.paidAt"), after.asString());
        } finally {
            logout(admin);
        }
    }

    private static String signatureHeader(String payload, String secret, long timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            StringBuilder signature = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                signature.append(String.format("%02x", value));
            }
            return "t=" + timestamp + ",v1=" + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign synthetic Stripe webhook", exception);
        }
    }
}
