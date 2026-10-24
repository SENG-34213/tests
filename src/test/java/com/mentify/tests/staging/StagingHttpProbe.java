package com.mentify.tests.staging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** HTTP probe that never follows redirects and never retains response bodies. */
final class StagingHttpProbe {

    private static final int MAX_JSON_BYTES = 1_048_576;

    record JsonResponse(int status, String contentType, byte[] body) {
    }

    private final HttpClient client;
    private final Duration requestTimeout;

    StagingHttpProbe(Duration connectTimeout, Duration requestTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.requestTimeout = requestTimeout;
    }

    int getStatus(URI endpoint) {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(requestTimeout).GET().build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException exception) {
            throw new IllegalStateException("Staging probe failed to connect or timed out", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Staging probe interrupted", exception);
        }
    }

    JsonResponse getJson(URI endpoint) {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(requestTimeout).GET().build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                byte[] bytes = body.readNBytes(MAX_JSON_BYTES + 1);
                if (bytes.length > MAX_JSON_BYTES) {
                    throw new IllegalStateException("Staging JSON response exceeded the 1 MiB safety limit");
                }
                return new JsonResponse(response.statusCode(),
                        response.headers().firstValue("Content-Type").orElse(""), bytes);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Staging probe failed to connect or timed out", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Staging probe interrupted", exception);
        }
    }
}
