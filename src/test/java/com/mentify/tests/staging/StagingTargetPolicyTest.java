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
        assertEquals("https://login.staging.example.test/realms/mentify/.well-known/openid-configuration",
                policy.keycloakDiscovery().toString());
        assertEquals("abcdef0123456", policy.deploymentRevision());
    }

    @Test
    void rejectsMissingOrIncorrectConfirmationBeforeAnyRequest() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_CONFIRMATION", "");

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
                "https://api.production.example.test"
        }) {
            Map<String, String> environment = validEnvironment();
            environment.put("STAGING_BASE_URL", target);
            assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment),
                    "Expected unsafe target to be rejected: " + target.replaceAll("password|secret", "[redacted]"));
        }
    }

    @Test
    void rejectsInvalidDeploymentRevisionAndRealmPathInjection() {
        Map<String, String> environment = validEnvironment();
        environment.put("STAGING_DEPLOYMENT_REVISION", "latest");
        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(environment));

        Map<String, String> invalidRealm = validEnvironment();
        invalidRealm.put("KEYCLOAK_REALM", "../admin");
        assertThrows(IllegalArgumentException.class, () -> StagingTargetPolicy.fromEnvironment(invalidRealm));
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
        environment.put("STAGING_KEYCLOAK_BASE_URL", "https://login.staging.example.test");
        environment.put("KEYCLOAK_REALM", "mentify");
        environment.put("STAGING_DEPLOYMENT_REVISION", "abcdef0123456");
        return environment;
    }
}
