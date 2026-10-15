package com.mentify.tests.attendance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttendanceClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new AttendanceClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new AttendanceClient(apiClient));
    }

    @Test
    void clientRejectsBlankIdentifiers() {
        AttendanceClient client = new AttendanceClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.listSessions(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.getSession("", null));
        assertThrows(IllegalArgumentException.class, () -> client.markAttendance(null, Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> client.mySummary("", null));
        assertThrows(IllegalArgumentException.class, () -> client.studentSummary("student", " ", null));
    }
}
