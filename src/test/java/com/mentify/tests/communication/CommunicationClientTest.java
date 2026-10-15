package com.mentify.tests.communication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CommunicationClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new CommunicationClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new CommunicationClient(apiClient));
    }

    @Test
    void clientRejectsInvalidIdentifiersAndPagination() {
        CommunicationClient client = new CommunicationClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.getGroup(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.listMembers(null, null));
        assertThrows(IllegalArgumentException.class, () -> client.messageHistory("group", -1, 10, null));
        assertThrows(IllegalArgumentException.class, () -> client.messageHistory("group", 0, 0, null));
        assertThrows(IllegalArgumentException.class, () -> client.deleteMessage("group", "message", " ", null));
    }
}
