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
