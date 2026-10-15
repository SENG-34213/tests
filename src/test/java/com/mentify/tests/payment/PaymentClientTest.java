package com.mentify.tests.payment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.ApiClientConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class PaymentClientTest {

    private final ApiClient apiClient = new ApiClient(new ApiClientConfiguration(
            URI.create("http://localhost:8080"), Duration.ofSeconds(1), Duration.ofSeconds(1), false));

    @Test
    void clientRequiresApiClient() {
        assertThrows(NullPointerException.class, () -> new PaymentClient(null));
    }

    @Test
    void clientCanBeConstructed() {
        assertNotNull(new PaymentClient(apiClient));
    }

    @Test
    void clientRejectsInvalidIdentifiersAndPagination() {
        PaymentClient client = new PaymentClient(apiClient);
        assertThrows(IllegalArgumentException.class, () -> client.myPayment(" ", null));
        assertThrows(IllegalArgumentException.class, () -> client.verifySuccessfulPayment(null, "course", null));
        assertThrows(IllegalArgumentException.class, () -> client.adminPayments(null, null, null, null, null,
                null, -1, 20, null));
        assertThrows(IllegalArgumentException.class, () -> client.adminPayments(null, null, null, null, null,
                null, 0, 0, null));
    }
}
