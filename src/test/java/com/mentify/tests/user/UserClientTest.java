package com.mentify.tests.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new UserClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new UserClient(apiClient));
    }

    @Test
    void clientRejectsBlankUserIdentifiers() {
        UserClient client = new UserClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.updateStatus(" ", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> client.resendInvitation(null, null));
        assertThrows(IllegalArgumentException.class, () -> client.studentExists("", null));
    }
}
