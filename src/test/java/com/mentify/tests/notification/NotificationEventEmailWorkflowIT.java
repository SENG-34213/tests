package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.user.MailpitTestClient;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

class NotificationEventEmailWorkflowIT extends NotificationIntegrationTestSupport {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void supportedKafkaEventCreatesNotificationAndSendsEmailToLocalMailpit() throws Exception {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_NOTIFICATION_EVENT_ENABLED", "false")),
                "Set TEST_NOTIFICATION_EVENT_ENABLED=true to publish a synthetic notification event");
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "NOTIFICATION_EMAIL_TEST_MODE", "false")),
                "Confirm backend notification SMTP points only to local Mailpit with NOTIFICATION_EMAIL_TEST_MODE=true");

        String bootstrapServers = requiredEnvironment("NOTIFICATION_KAFKA_BOOTSTRAP_SERVERS");
        String topic = System.getenv().getOrDefault("NOTIFICATION_KAFKA_TOPIC", "mentify.notification-events");
        MailpitTestClient mailpit = MailpitTestClient.fromEnvironment();
        AuthenticationSession student = login(TestRole.STUDENT);
        String token = student.accessToken();
        String recipientUserId = jwtSubject(token);
        String recipientEmail = "notification-" + UUID.randomUUID() + "@example.invalid";
        String title = "Synthetic notification " + UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Set<String> existingMailIds = mailpit.messageIdsForRecipient(recipientEmail);
        String notificationId = null;

        try {
            publishEvent(bootstrapServers, topic, eventId, recipientUserId, recipientEmail, title);
            JsonNode notification = awaitNotification(token, eventId, title);
            notificationId = notification.path("id").asText();
            assertNotNull(notificationId);
            assertTrue(!notificationId.isBlank(), "Ingested event must have a notification ID");
            assertEquals("QUIZ_CREATED", notification.path("type").asText());
            awaitEmailStatus(token, title, eventId);
            notification = findNotification(token, title);
            assertEquals("SENT", notification.path("emailStatus").asText(),
                    "Email must be accepted by the configured local Mailpit SMTP sink");

            String mailId = mailpit.awaitNewMessageForRecipient(recipientEmail, existingMailIds);
            mailpit.assertMessageHasContent(mailId);
            assertTrue(mailpit.messageIdsForRecipient(recipientEmail).contains(mailId));
        } finally {
            try {
                if (notificationId != null && !notificationId.isBlank()) {
                    Response deleted = notifications.delete(notificationId, token);
                    assertEquals(200, deleted.statusCode(), deleted.asString());
                }
            } finally {
                try {
                    Set<String> currentMailIds = mailpit.messageIdsForRecipient(recipientEmail);
                    currentMailIds.removeAll(existingMailIds);
                    mailpit.deleteMessages(currentMailIds);
                } finally {
                    logout(student);
                }
            }
        }
    }

    private static void publishEvent(String bootstrapServers, String topic, UUID eventId,
                                     String recipientUserId, String recipientEmail, String title) throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("recipientUserId", recipientUserId);
        event.put("recipientEmail", recipientEmail);
        event.put("type", "QUIZ_CREATED");
        event.put("title", title);
        event.put("message", "Synthetic integration test event; no real recipient address is used.");
        event.put("sourceService", "test-repository");
        event.put("occurredAt", Instant.now().toString());
        String payload = JSON.writeValueAsString(event);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties(bootstrapServers))) {
            producer.send(new ProducerRecord<>(topic, eventId.toString(), payload))
                    .get(Duration.ofSeconds(15).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    private static Map<String, Object> producerProperties(String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        properties.put("acks", "all");
        properties.put("enable.idempotence", true);
        properties.put("request.timeout.ms", 15000);
        properties.put("delivery.timeout.ms", 20000);
        String securityProtocol = System.getenv("NOTIFICATION_KAFKA_SECURITY_PROTOCOL");
        if (securityProtocol != null && !securityProtocol.isBlank()) {
            properties.put("security.protocol", securityProtocol.trim());
        }
        String mechanism = System.getenv("NOTIFICATION_KAFKA_SASL_MECHANISM");
        String jaas = System.getenv("NOTIFICATION_KAFKA_SASL_JAAS_CONFIG");
        assumeTrue((mechanism == null || mechanism.isBlank()) == (jaas == null || jaas.isBlank()),
                "Set both notification Kafka SASL mechanism and JAAS config or neither");
        if (mechanism != null && !mechanism.isBlank()) {
            properties.put("sasl.mechanism", mechanism.trim());
            properties.put("sasl.jaas.config", jaas);
        }
        return properties;
    }

    private static JsonNode awaitNotification(String token, UUID eventId, String title) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(50).toNanos();
        while (System.nanoTime() < deadline) {
            JsonNode notification = findNotificationOrNull(token, title);
            if (notification != null) {
                return notification;
            }
            Thread.sleep(500);
        }
        throw new AssertionError("Kafka event was not visible in the student's notification list: " + eventId);
    }

    private static void awaitEmailStatus(String token, String title, UUID eventId) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(90).toNanos();
        while (System.nanoTime() < deadline) {
            JsonNode notification = findNotification(token, title);
            String status = notification.path("emailStatus").asText();
            if ("SENT".equals(status)) {
                return;
            }
            if ("FAILED".equals(status)) {
                throw new AssertionError("Email delivery failed for synthetic event " + eventId);
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("Email delivery did not reach SENT for synthetic event " + eventId);
    }

    private static JsonNode findNotification(String token, String title) {
        JsonNode notification = findNotificationOrNull(token, title);
        if (notification == null) {
            throw new AssertionError("Ingested notification was not found for its unique synthetic title");
        }
        return notification;
    }

    private static JsonNode findNotificationOrNull(String token, String title) {
        Response page = notifications.list(0, 100, false, token);
        assertEquals(200, page.statusCode(), page.asString());
        for (JsonNode notification : JSON.valueToTree(
                page.jsonPath().getList("data.notifications", Map.class))) {
            if (title.equals(notification.path("title").asText())) {
                return notification;
            }
        }
        return null;
    }

    private static String jwtSubject(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Student access token is not a JWT");
        }
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        String subject = JSON.readTree(new String(payload, StandardCharsets.UTF_8)).path("sub").asText();
        if (subject.isBlank()) {
            throw new IllegalArgumentException("Student access token has no subject claim");
        }
        return subject;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        assumeTrue(value != null && !value.isBlank(), "Set " + name + " to the non-production test Kafka broker");
        return value.trim();
    }
}
