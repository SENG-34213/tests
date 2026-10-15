package com.mentify.tests.notification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class NotificationClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new NotificationClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new NotificationClient(apiClient));
    }

    @Test
    void clientRejectsInvalidPaginationAndIdentifiers() {
        NotificationClient client = new NotificationClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.list(-1, 20, false, null));
        assertThrows(IllegalArgumentException.class, () -> client.list(0, 0, false, null));
        assertThrows(IllegalArgumentException.class, () -> client.list(0, 101, false, null));
        assertThrows(IllegalArgumentException.class, () -> client.markRead(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.delete(null, null));
    }
}
