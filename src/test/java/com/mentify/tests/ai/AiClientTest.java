package com.mentify.tests.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AiClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new AiClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new AiClient(apiClient));
    }

    @Test
    void clientRejectsBlankResourceIdentifiers() {
        AiClient client = new AiClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.conversation(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.messages(null, null));
        assertThrows(IllegalArgumentException.class, () -> client.summarizeLesson("", null));
        assertThrows(IllegalArgumentException.class, () -> client.summarizeMaterial(" ", null));
    }
}
