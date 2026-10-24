package com.mentify.tests.websocket;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;

/** Minimal STOMP 1.2 client for non-mutating authenticated CONNECT smoke tests. */
public final class StompWebSocketProbe {

    private static final String CLOSED_EVENT = "__WEBSOCKET_CLOSED__";
    private static final String ERROR_EVENT = "__WEBSOCKET_CLIENT_ERROR__";

    private StompWebSocketProbe() {
    }

    public static void assertAuthenticatedConnect(URI endpoint, String accessToken) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connection.send(connectFrame(accessToken));
            String response = connection.nextFrame();
            assertTrue(response != null && response.startsWith("CONNECTED\n"),
                    "Expected STOMP CONNECTED frame, received: " + safeFrame(response));
        } finally {
            connection.close();
        }
    }

    public static void assertUnauthenticatedConnectRejected(URI endpoint) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connection.send("CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\u0000");
            String response = connection.nextFrame();
            assertRejected(response, "unauthenticated STOMP CONNECT");
        } finally {
            connection.close();
        }
    }

    public static void assertConnectRejected(URI endpoint, String accessToken) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connection.send(connectFrame(accessToken));
            assertRejected(connection.nextFrame(), "STOMP CONNECT for a role not allowed by the endpoint");
        } finally {
            connection.close();
        }
    }

    public static void assertGroupSubscriptionRejected(URI endpoint, String accessToken, UUID groupId)
            throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            connection.send("SUBSCRIBE\nid:unauthorized-group-subscription\ndestination:/topic/groups/"
                    + groupId + "\nack:auto\n\n\u0000");
            assertRejected(connection.nextFrame(), "subscription to a group without active membership");
        } finally {
            connection.close();
        }
    }

    public static String assertGroupMessageRoundTrip(
            URI endpoint, String accessToken, UUID groupId, String content) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            String receiptId = "group-subscription-" + UUID.randomUUID();
            connection.send("SUBSCRIBE\nid:group-round-trip\ndestination:/topic/groups/" + groupId
                    + "\nack:auto\nreceipt:" + receiptId + "\n\n\u0000");
            String receipt = connection.nextFrame();
            assertTrue(receipt != null && receipt.startsWith("RECEIPT\n")
                            && receipt.contains("receipt-id:" + receiptId),
                    "Expected group subscription receipt, received: " + safeFrame(receipt));

            connection.send("SEND\ndestination:/app/groups/" + groupId + "/messages"
                    + "\ncontent-type:application/json\n\n{\"content\":\"" + content + "\"}\u0000");
            String message = connection.nextFrame();
            assertTrue(message != null && message.startsWith("MESSAGE\n")
                            && message.contains("\"groupId\":\"" + groupId + "\"")
                            && message.contains("\"content\":\"" + content + "\""),
                    "Expected the active group member to receive the persisted group broadcast, received: "
                            + safeFrame(message));
            String body = message.substring(message.indexOf("\n\n") + 2);
            Matcher idMatcher = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([0-9a-fA-F-]{36})\\\"")
                    .matcher(body);
            assertTrue(idMatcher.find(), "Expected broadcast message ID to support API cleanup");
            return idMatcher.group(1);
        } finally {
            connection.close();
        }
    }

    public static void assertGroupSendRejected(URI endpoint, String accessToken, UUID groupId) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            connection.send("SEND\ndestination:/app/groups/" + groupId + "/messages"
                    + "\ncontent-type:application/json\n\n{\"content\":\"authorization-probe\"}\u0000");
            assertRejected(connection.nextFrame(), "send to a group without active membership");
        } finally {
            connection.close();
        }
    }

    public static void assertDestinationRejected(
            URI endpoint, String accessToken, String command, String destination) throws Exception {
        if (!"SEND".equals(command) && !"SUBSCRIBE".equals(command)) {
            throw new IllegalArgumentException("Only SEND and SUBSCRIBE destination probes are supported");
        }
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            String headers = command + "\n" + ("SUBSCRIBE".equals(command) ? "id:denied-destination\n" : "")
                    + "destination:" + destination + "\n";
            connection.send(headers + ("SEND".equals(command) ? "content-type:application/json\n\n{}" : "\n")
                    + "\u0000");
            assertRejected(connection.nextFrame(), command + " to unauthorized destination " + destination);
        } finally {
            connection.close();
        }
    }

    public static void assertAiConversationErrorDelivered(
            URI endpoint, String accessToken, UUID unknownConversationId, String clientMessageId) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            String receiptId = "ai-chat-" + UUID.randomUUID();
            connection.send("SUBSCRIBE\nid:ai-chat-response\ndestination:/user/queue/ai/chat\nack:auto\nreceipt:"
                    + receiptId + "\n\n\u0000");
            String receipt = connection.nextFrame();
            assertTrue(receipt != null && receipt.startsWith("RECEIPT\n")
                            && receipt.contains("receipt-id:" + receiptId),
                    "Expected AI response subscription receipt, received: " + safeFrame(receipt));

            String payload = "{\"conversationId\":\"" + unknownConversationId + "\",\"clientMessageId\":\""
                    + clientMessageId + "\",\"message\":\"safe integration probe\"}";
            connection.send("SEND\ndestination:/app/ai/chat\ncontent-type:application/json\n\n"
                    + payload + "\u0000");
            String processing = connection.nextFrame();
            assertTrue(processing != null && processing.startsWith("MESSAGE\n")
                            && processing.contains("\"status\":\"PROCESSING\"")
                            && processing.contains(clientMessageId),
                    "Expected AI PROCESSING update for this client message, received: " + safeFrame(processing));
            String error = connection.nextFrame();
            assertTrue(error != null && error.startsWith("MESSAGE\n")
                            && error.contains("\"status\":\"ERROR\"")
                            && error.contains("AI conversation not found"),
                    "Expected safe unknown-conversation error on the user queue, received: " + safeFrame(error));
        } finally {
            connection.close();
        }
    }

    public static void assertTestTopicRoundTrip(URI endpoint, String accessToken, String content) throws Exception {
        ProbeConnection connection = open(endpoint);
        try {
            connectAndAssert(connection, accessToken);
            String receiptId = "test-topic-" + UUID.randomUUID();
            connection.send("SUBSCRIBE\nid:round-trip\ndestination:/topic/test\nack:auto\nreceipt:"
                    + receiptId + "\n\n\u0000");
            String receipt = connection.nextFrame();
            assertTrue(receipt != null && receipt.startsWith("RECEIPT\n")
                            && receipt.contains("receipt-id:" + receiptId),
                    "Expected subscription receipt, received: " + safeFrame(receipt));

            connection.send("SEND\ndestination:/app/test\ncontent-type:application/json\n\n"
                    + "{\"content\":\"" + content + "\"}\u0000");
            String message = connection.nextFrame();
            assertTrue(message != null && message.startsWith("MESSAGE\n")
                            && message.contains("\"content\":\"" + content + "\""),
                    "Expected the test-topic broadcast to contain the submitted content, received: "
                            + safeFrame(message));
        } finally {
            connection.close();
        }
    }

    public static URI endpoint(RequiredService service, String path) {
        URI base = TestEnvironmentConfiguration.fromEnvironment().serviceUrls().get(service);
        String scheme = base.getScheme().equals("https") ? "wss" : "ws";
        String basePath = base.getRawPath() == null ? "" : base.getRawPath();
        return URI.create(scheme + "://" + base.getRawAuthority() + basePath + path);
    }

    private static ProbeConnection open(URI endpoint) throws Exception {
        StompFrameListener listener = new StompFrameListener();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        WebSocket socket = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(endpoint, listener)
                .get(15, TimeUnit.SECONDS);
        return new ProbeConnection(socket, listener);
    }

    private static String connectFrame(String accessToken) {
        return "CONNECT\naccept-version:1.2\nheart-beat:0,0\nAuthorization:Bearer "
                + accessToken + "\n\n\u0000";
    }

    private static void connectAndAssert(ProbeConnection connection, String accessToken) throws Exception {
        connection.send(connectFrame(accessToken));
        String response = connection.nextFrame();
        assertTrue(response != null && response.startsWith("CONNECTED\n"),
                "Expected STOMP CONNECTED frame, received: " + safeFrame(response));
    }

    private static void assertRejected(String response, String operation) {
        assertTrue(response != null && !response.startsWith("CONNECTED\n")
                        && (response.startsWith("ERROR\n") || CLOSED_EVENT.equals(response)
                        || ERROR_EVENT.equals(response)),
                "Expected STOMP to reject " + operation + ", received: " + safeFrame(response));
    }

    private static String safeFrame(String frame) {
        if (frame == null) {
            return "no STOMP frame before timeout";
        }
        if (CLOSED_EVENT.equals(frame)) {
            return "WebSocket closed";
        }
        if (ERROR_EVENT.equals(frame)) {
            return "WebSocket client error";
        }
        int end = frame.indexOf('\n');
        return end < 0 ? frame : frame.substring(0, end);
    }

    private static final class ProbeConnection {
        private final WebSocket socket;
        private final StompFrameListener listener;

        private ProbeConnection(WebSocket socket, StompFrameListener listener) {
            this.socket = socket;
            this.listener = listener;
        }

        private void send(String frame) throws Exception {
            socket.sendText(frame, true).get(5, TimeUnit.SECONDS);
        }

        private String nextFrame() throws InterruptedException {
            return listener.frames().poll(10, TimeUnit.SECONDS);
        }

        private void close() {
            socket.abort();
        }
    }

    private static final class StompFrameListener implements WebSocket.Listener {
        private final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
        private final StringBuilder currentFrame = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            currentFrame.append(data);
            int terminator = currentFrame.indexOf("\u0000");
            while (terminator >= 0) {
                frames.offer(currentFrame.substring(0, terminator));
                currentFrame.delete(0, terminator + 1);
                terminator = currentFrame.indexOf("\u0000");
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            frames.offer(CLOSED_EVENT);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            frames.offer(ERROR_EVENT);
        }

        BlockingQueue<String> frames() {
            return frames;
        }
    }
}
