package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class MaterialDownloadUrlPolicyTest {

    @Test
    void acceptsOnlyHttpsOnTheExactConfiguredHost() {
        URI uri = MaterialDownloadUrlPolicy.validate(
                "https://storage.example.test/object?signature=synthetic", "storage.example.test");

        assertEquals("storage.example.test", uri.getHost());
    }

    @Test
    void rejectsUnconfiguredHostsAndNonHttpsSchemes() {
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "https://other.example.test/object", "storage.example.test"));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "http://storage.example.test/object", "storage.example.test"));
    }

    @Test
    void allowsHttpOnlyForAnExplicitLoopbackOriginWhenEnabled() {
        URI uri = MaterialDownloadUrlPolicy.validate(
                "http://localhost:9000/object?signature=synthetic", "localhost:9000", true);

        assertEquals("localhost", uri.getHost());
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "http://localhost:9001/object", "localhost:9000", true));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "http://storage.example.test:9000/object", "storage.example.test:9000", true));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "http://localhost:9000/object", "localhost:9000", false));
    }

    @Test
    void rejectsCredentialsFragmentsAndUnapprovedPorts() {
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "https://user@storage.example.test/object", "storage.example.test"));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "https://storage.example.test:8443/object", "storage.example.test"));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "https://storage.example.test/object#fragment", "storage.example.test"));
    }

    @Test
    void requiresAnExplicitHostAndNonEmptyDownloadUrl() {
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(
                "https://storage.example.test/object", " "));
        assertThrows(IllegalArgumentException.class, () -> MaterialDownloadUrlPolicy.validate(" ",
                "storage.example.test"));
    }
}
