package com.mentify.tests.staging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class StagingHttpProbeTest {

    @Test
    void doesNotFollowRedirectsToAnotherHost() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Location", "https://example.org/collect");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        try {
            StagingHttpProbe probe = new StagingHttpProbe(Duration.ofSeconds(1), Duration.ofSeconds(1));
            assertEquals(302, probe.getStatus(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/")));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void appliesRequestTimeoutWithoutReadingResponseBody() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(500);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            StagingHttpProbe probe = new StagingHttpProbe(Duration.ofSeconds(1), Duration.ofMillis(100));
            assertThrows(IllegalStateException.class,
                    () -> probe.getStatus(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/")));
        } finally {
            server.stop(0);
        }
    }
}
