package com.mentify.tests.staging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StagingTargetPolicyTest {

    @Test
    void acceptsConfirmedHttpsHostWithinConfiguredStagingSuffix() {
        StagingTargetPolicy policy = StagingTargetPolicy.fromEnvironment(validEnvironment());

        assertEquals("https://api.staging.example.test/", policy.gatewayBase().toString());
        assertEquals("https://auth.staging.example.test/", policy.authServiceBase().toString());
        assertEquals("https://auth.staging.example.test/api/v1/auth/.well-known/jwks.json",
                policy.jwtKeySet().toString());
        assertEquals("abcdef0123456", policy.deploymentRevision());
    }

    @Test
    void rejectsMissingOrIncorrectConfirmationBeforeAnyRequest() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_CONFIRMATION", "");

        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));
        environment.put("STAGING_CONFIRMATION", "wrong-confirmation");
        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));
    }

    @Test
    void rejectsNonStagingEnvironment() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_ENVIRONMENT", "production");

        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));
    }

    @Test
    void rejectsHttpEmbeddedCredentialsQueryAndUnapprovedHosts() {
        for (String target : new String[] {
                "http://api.staging.example.test",
                "https://user:password@api.staging.example.test",
                "https://api.staging.example.test?token=secret",
                "https://api.staging.example.test:8443",
                "https://api.production.example.test"
        }) {
            Map<String, String> environment = validEnvironment();
            environment.put("STAGING_BASE_URL", target);
            assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment),
                    "Expected unsafe target to be rejected: " + target.replaceAll("password|secret", "[redacted]"));
        }

        Map<String, String> invalidAuthTarget = validEnvironment();
        invalidAuthTarget.put("STAGING_AUTH_SERVICE_BASE_URL", "https://auth.production.example.test");
        assertThrows(IllegalArgumentException.class,
                () -> StagingTargetPolicy.fromEnvironment(invalidAuthTarget));
    }

    @Test
    void rejectsInvalidDeploymentRevision() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_DEPLOYMENT_REVISION", "latest");
        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));
    }

    @Test
    void rejectsIpAddressAsStagingHostSuffix() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_ALLOWED_HOST_SUFFIX", "127.0.0.1");

        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));
    }

    private static Map<String, String> validEnvironment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("STAGING_ENVIRONMENT", "staging");
        environment.put("STAGING_CONFIRMATION", StagingTargetPolicy.REQUIRED_CONFIRMATION);
        environment.put("STAGING_ALLOWED_HOST_SUFFIX", "staging.example.test");
        environment.put("STAGING_BASE_URL", "https://api.staging.example.test");
        environment.put("STAGING_AUTH_SERVICE_BASE_URL", "https://auth.staging.example.test");
        environment.put("STAGING_DEPLOYMENT_REVISION", "abcdef0123456");
        return environment;
    }
}
