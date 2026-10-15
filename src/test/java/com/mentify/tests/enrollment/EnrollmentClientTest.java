package com.mentify.tests.enrollment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnrollmentClientTest {

    @Test
    void shouldRejectNullApiClient() {
        assertThrows(NullPointerException.class, () -> new EnrollmentClient(null));
    }

    @Test
    void shouldCreateClientWithConfiguredApiClient() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);

        EnrollmentClient client = new EnrollmentClient(new ApiClient(configuration));

        assertNotNull(client);
    }

    @Test
    void shouldRejectBlankPathIdentifiers() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);
        EnrollmentClient client = new EnrollmentClient(new ApiClient(configuration));

        assertThrows(IllegalArgumentException.class, () -> client.update(" ", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> client.membership("student", "", null));
        assertThrows(IllegalArgumentException.class, () -> client.studentCourses(null, null));
    }
}
