package com.mentify.tests.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;

public final class MailpitTestClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final URI baseUri;
    private final HttpClient client;
    private final String authorization;

    private MailpitTestClient(URI baseUri, String authorization) {
        this.baseUri = baseUri;
        this.authorization = authorization;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public static MailpitTestClient fromEnvironment() {
        String configured = System.getenv("MAILPIT_API_BASE_URL");
        Assumptions.assumeTrue(configured != null && !configured.isBlank(),
                "Set MAILPIT_API_BASE_URL to the local Mailpit API");
        URI uri = URI.create(configured.trim());
        String host = uri.getHost();
        Assumptions.assumeTrue("http".equalsIgnoreCase(uri.getScheme())
                        && host != null && (host.equalsIgnoreCase("localhost")
                        || host.equals("127.0.0.1") || host.equals("::1"))
                        && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null,
                "Mailpit test API must use a local HTTP host without embedded credentials");
        String username = System.getenv("MAILPIT_API_USERNAME");
        String password = System.getenv("MAILPIT_API_PASSWORD");
        Assumptions.assumeTrue((username == null || username.isBlank()) == (password == null || password.isBlank()),
                "Set both MAILPIT_API_USERNAME and MAILPIT_API_PASSWORD or neither");
        String authorization = username == null || username.isBlank() ? null
                : "Basic " + java.util.Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/" : uri.getPath();
        URI normalized = URI.create(uri.getScheme() + "://" + uri.getRawAuthority()
                + (path.endsWith("/") ? path : path + "/"));
        return new MailpitTestClient(normalized, authorization);
    }

    public Set<String> messageIdsForRecipient(String email) {
        String query = "api/v1/messages?start=0&limit=100";
        JsonNode response = getJson(query);
        Set<String> ids = new HashSet<>();
        for (JsonNode message : response.path("messages")) {
            for (JsonNode recipient : message.path("To")) {
                if (email.equalsIgnoreCase(recipient.path("Address").asText())) {
                    ids.add(message.path("ID").asText());
                }
            }
        }
        return ids;
    }

    public String awaitNewMessageForRecipient(String email, Set<String> existingIds) {
        long deadline = System.nanoTime() + Duration.ofSeconds(50).toNanos();
        while (System.nanoTime() < deadline) {
            Set<String> current = messageIdsForRecipient(email);
            current.removeAll(existingIds);
            if (!current.isEmpty()) {
                return current.iterator().next();
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for a captured test email");
            }
        }
        throw new AssertionError("No new email for the synthetic recipient appeared in local Mailpit");
    }

    public void assertMessageHasContent(String messageId) {
        JsonNode message = getJson("api/v1/message/" + encodePath(messageId));
        String text = message.path("Text").asText();
        String html = message.path("HTML").asText();
        if (text.isBlank() && html.isBlank()) {
            throw new AssertionError("Captured test email has no text or HTML body");
        }
    }

    public String messageText(String messageId) {
        JsonNode message = getJson("api/v1/message/" + encodePath(messageId));
        return message.path("Text").asText() + "\n" + message.path("HTML").asText();
    }

    public void deleteMessage(String messageId) {
        deleteMessages(Set.of(messageId));
    }

    public void deleteMessages(Set<String> messageIds) {
        if (messageIds.isEmpty()) {
            return;
        }
        var ids = JSON.createObjectNode().putArray("IDs");
        messageIds.forEach(ids::add);
        JsonNode body = ids;
        HttpRequest.Builder builder = request("api/v1/messages")
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(body.toString()));
        send(builder.build(), 200);
    }

    private JsonNode getJson(String path) {
        HttpRequest.Builder builder = request(path).GET();
        HttpResponse<String> response = send(builder.build(), 200);
        try {
            return JSON.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Mailpit returned an invalid JSON response");
        }
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(Duration.ofSeconds(5)).header("Accept", "application/json");
        if (authorization != null) {
            builder.header("Authorization", authorization);
        }
        return builder;
    }

    private HttpResponse<String> send(HttpRequest request, int expectedStatus) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != expectedStatus) {
                throw new IllegalStateException("Mailpit API returned HTTP " + response.statusCode());
            }
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to reach local Mailpit API");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling local Mailpit API");
        }
    }

    private static String encodePath(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
