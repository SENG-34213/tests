package com.mentify.tests.calendar;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CalendarClientTest {

    @Test
    void shouldRejectNullApiClient() {
        assertThrows(NullPointerException.class, () -> new CalendarClient(null));
    }

    @Test
    void shouldCreateClientWithConfiguredApiClient() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);

        CalendarClient client = new CalendarClient(new ApiClient(configuration));

        org.junit.jupiter.api.Assertions.assertNotNull(client);
    }

    @Test
    void shouldRejectBlankEventIdForUpdateAndCancel() {
        ApiClientConfiguration configuration = new ApiClientConfiguration(
                URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false);
        CalendarClient client = new CalendarClient(new ApiClient(configuration));

        assertThrows(IllegalArgumentException.class, () -> client.update(" ", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> client.cancel("", null));
    }
}
