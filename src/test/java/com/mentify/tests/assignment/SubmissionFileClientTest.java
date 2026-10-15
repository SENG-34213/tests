package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SubmissionFileClientTest {

    private static final ApiClient CLIENT = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofMillis(100), Duration.ofMillis(100), false));

    @Test
    void rejectsMissingSubmissionId() {
        SubmissionFileClient client = new SubmissionFileClient(CLIENT);
        assertThrows(IllegalArgumentException.class, () -> client.list(null, null));
    }

    @Test
    void rejectsMissingFileId() {
        SubmissionFileClient client = new SubmissionFileClient(CLIENT);
        assertThrows(IllegalArgumentException.class, () -> client.delete("submission", "", null));
    }

    @Test
    void rejectsNullApiClient() {
        assertThrows(NullPointerException.class, () -> new SubmissionFileClient(null));
    }
}
