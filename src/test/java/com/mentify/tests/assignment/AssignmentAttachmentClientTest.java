package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AssignmentAttachmentClientTest {

    private static final ApiClient CLIENT = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofMillis(100), Duration.ofMillis(100), false));

    @Test
    void rejectsMissingAssignmentId() {
        AssignmentAttachmentClient client = new AssignmentAttachmentClient(CLIENT);
        assertThrows(IllegalArgumentException.class, () -> client.list("", null));
    }

    @Test
    void rejectsMissingAttachmentId() {
        AssignmentAttachmentClient client = new AssignmentAttachmentClient(CLIENT);
        assertThrows(IllegalArgumentException.class, () -> client.download("assignment", " ", null));
    }

    @Test
    void rejectsNullApiClient() {
        assertThrows(NullPointerException.class, () -> new AssignmentAttachmentClient(null));
    }
}
