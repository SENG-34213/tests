package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AssignmentClientTest {

    @Test
    void requiresApiClient() {
        assertThrows(NullPointerException.class, () -> new AssignmentClient(null));
    }

    @Test
    void createsAssignmentClient() {
        ApiClient client = new ApiClient(new ApiClientConfiguration(
                java.net.URI.create("http://localhost:8080"),
                Duration.ofMillis(100), Duration.ofMillis(100), false));

        assertNotNull(new AssignmentClient(client));
    }

    @Test
    void rejectsBlankAssignmentAndCourseIdentifiers() {
        ApiClient client = new ApiClient(new ApiClientConfiguration(
                java.net.URI.create("http://localhost:8080"),
                Duration.ofMillis(100), Duration.ofMillis(100), false));
        AssignmentClient assignmentClient = new AssignmentClient(client);

        assertThrows(IllegalArgumentException.class, () -> assignmentClient.get(" ", null));
        assertThrows(IllegalArgumentException.class, () -> assignmentClient.listForCourse("", null));
    }
}
