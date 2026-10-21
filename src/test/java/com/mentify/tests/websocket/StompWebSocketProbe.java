package com.mentify.tests.websocket;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;

/** Minimal STOMP 1.2 client for non-mutating authenticated CONNECT smoke tests. */
public final class StompWebSocketProbe {

    private StompWebSocketProbe() {
    }

    public static void assertAuthenticatedConnect(URI endpoint, String accessToken) throws Exception {
        StompFrameListener listener = new StompFrameListener();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        WebSocket socket = client.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(endpoint, listener)
                .get(15, TimeUnit.SECONDS);
        try {
            socket.sendText("CONNECT\naccept-version:1.2\nheart-beat:0,0\nAuthorization:Bearer "
                    + accessToken + "\n\n\u0000", true).get(5, TimeUnit.SECONDS);
            String response = listener.frames().poll(10, TimeUnit.SECONDS);
            assertTrue(response != null && response.startsWith("CONNECTED\n"),
                    "Expected STOMP CONNECTED frame, received: " + safeFrame(response));
            socket.sendText("DISCONNECT\n\n\u0000", true).get(5, TimeUnit.SECONDS);
        } finally {
            if (!socket.isOutputClosed() && !socket.isInputClosed()) {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "test complete").get(5, TimeUnit.SECONDS);
            }
        }
    }

    public static URI endpoint(RequiredService service, String path) {
        URI base = TestEnvironmentConfiguration.fromEnvironment().serviceUrls().get(service);
        String scheme = base.getScheme().equals("https") ? "wss" : "ws";
        String basePath = base.getRawPath() == null ? "" : base.getRawPath();
        return URI.create(scheme + "://" + base.getRawAuthority() + basePath + path);
    }

    private static String safeFrame(String frame) {
        if (frame == null) {
            return "no STOMP frame before timeout";
        }
        int end = frame.indexOf('\n');
        return end < 0 ? frame : frame.substring(0, end);
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
            if (terminator >= 0) {
                frames.offer(currentFrame.substring(0, terminator));
                currentFrame.delete(0, currentFrame.length());
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        BlockingQueue<String> frames() {
            return frames;
        }
    }
}
