package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MaterialClientTest {

    private static final ApiClient CLIENT = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8091"), Duration.ofMillis(100), Duration.ofMillis(100), false));

    @Test
    void rejectsMissingMaterialId() {
        MaterialClient client = new MaterialClient(CLIENT);
        assertThrows(IllegalArgumentException.class, () -> client.get("", null));
    }

    @Test
    void rejectsNullApiClient() {
        assertThrows(NullPointerException.class, () -> new MaterialClient(null));
    }
}
