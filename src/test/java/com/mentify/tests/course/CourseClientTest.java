package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CourseClientTest {

    @Test
    void shouldRejectNullApiClient() {
        assertThrows(NullPointerException.class, () -> new CourseClient(null));
    }

    @Test
    void shouldCreateClientWithConfiguredApiClient() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);

        CourseClient client = new CourseClient(new ApiClient(configuration));

        assertNotNull(client);
    }

    @Test
    void shouldRejectBlankCourseIdForLookupUpdateAndDelete() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);
        CourseClient client = new CourseClient(new ApiClient(configuration));

        assertThrows(IllegalArgumentException.class, () -> client.lookup(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.update("", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> client.delete(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.teacherSearch(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.teacherStudents(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.teacherGradebook(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.teacherAnalytics(" ", null));
        assertThrows(IllegalArgumentException.class,
                () -> client.teacherStudentSummary(" ", "course-id", null));
    }
}
