package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SubmissionClientTest {

    @Test
    void requiresApiClient() {
        assertThrows(NullPointerException.class, () -> new SubmissionClient(null));
    }

    @Test
    void createsSubmissionClient() {
        ApiClient client = new ApiClient(new ApiClientConfiguration(
                java.net.URI.create("http://localhost:8080"),
                Duration.ofMillis(100), Duration.ofMillis(100), false));

        assertNotNull(new SubmissionClient(client));
    }

    @Test
    void rejectsBlankSubmissionIdentifiers() {
        ApiClient client = new ApiClient(new ApiClientConfiguration(
                java.net.URI.create("http://localhost:8080"),
                Duration.ofMillis(100), Duration.ofMillis(100), false));
        SubmissionClient submissionClient = new SubmissionClient(client);

        assertThrows(IllegalArgumentException.class, () -> submissionClient.get(" ", null));
        assertThrows(IllegalArgumentException.class, () -> submissionClient.create("", Map.of(), null));
    }
}
