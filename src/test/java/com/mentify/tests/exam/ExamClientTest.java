package com.mentify.tests.exam;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ExamClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new ExamClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new ExamClient(apiClient));
    }

    @Test
    void clientRejectsBlankIdentifiers() {
        ExamClient client = new ExamClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.get(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.listByCourse(null, null));
        assertThrows(IllegalArgumentException.class, () -> client.schedule("", null));
        assertThrows(IllegalArgumentException.class, () -> client.results(" ", null));
    }
}
