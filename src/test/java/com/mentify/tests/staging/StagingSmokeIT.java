package com.mentify.tests.staging;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Manual-only, read-only smoke checks for the approved staging environment. */
class StagingSmokeIT {

    private static final Map<String, String> RESULTS = new ConcurrentHashMap<>();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static StagingTargetPolicy policy;
    private static StagingHttpProbe probe;
    private static Duration requestTimeout;
    private static Duration overallTimeout;
    private static Duration pollInterval;
    private static String workflowRun;
    private static Instant startedAt;

    @BeforeAll
    static void configure() {
        startedAt = Instant.now();
        policy = StagingTargetPolicy.fromEnvironment();
        requestTimeout = positiveSeconds("STAGING_REQUEST_TIMEOUT_SECONDS", 8);
        overallTimeout = positiveSeconds("STAGING_READINESS_TIMEOUT_SECONDS", 90);
        pollInterval = positiveSeconds("STAGING_POLL_INTERVAL_SECONDS", 3);
        probe = new StagingHttpProbe(Duration.ofSeconds(5), requestTimeout);
        workflowRun = System.getenv().getOrDefault("GITHUB_RUN_ID", "local-unbound");
    }

    @Test
    void gatewayHealthIsReadyWithinBoundedDeadline() {
        URI health = policy.gatewayBase().resolve("actuator/health");
        awaitSuccess("gateway-readiness", health, status -> status >= 200 && status < 300);
    }

    @Test
    void authServiceHealthIsReadyWithinBoundedDeadline() {
        awaitSuccess("auth-service-readiness", policy.authServiceHealth(),
                status -> status >= 200 && status < 300);
    }

    @Test
    void jwtSigningKeysArePublished() {
        StagingHttpProbe.JsonResponse response = probe.getJson(policy.jwtKeySet());
        assertTrue(response.status() == 200, "JWT key-set endpoint must return HTTP 200");
        assertTrue(response.contentType().toLowerCase().startsWith("application/json"),
                "JWT key-set endpoint must return JSON");
        try {
            JsonNode document = JSON.readTree(response.body());
            JsonNode keys = document.path("keys");
            assertTrue(keys.isArray() && !keys.isEmpty(), "JWT key set must contain at least one public key");
            JsonNode key = keys.get(0);
            assertTrue("RSA".equals(requiredField(key, "kty")), "JWT signing key must use RSA");
            assertTrue("RS256".equals(requiredField(key, "alg")), "JWT signing algorithm must be RS256");
            requiredField(key, "kid");
            requiredField(key, "n");
            requiredField(key, "e");
        } catch (IOException exception) {
            throw new AssertionError("JWT key-set response was not valid JSON", exception);
        }
        RESULTS.put("jwt-signing-key-set", "Passed (HTTP 200, public RS256 key fields present)");
    }

    @Test
    void protectedProbeRejectsAnonymousRequest() {
        URI endpoint = policy.gatewayBase().resolve("api/v1/auth/protected-test");
        int status = probe.getStatus(endpoint);
        assertTrue(status == 401 || status == 403,
                "Anonymous protected-route probe must return 401/403; received HTTP " + status);
        RESULTS.put("anonymous-protected-route", "Passed (HTTP " + status + ")");
    }

    @AfterAll
    static void writeSanitizedEvidence() throws IOException {
        Path evidence = Path.of("target", "staging-evidence", "summary.md");
        Files.createDirectories(evidence.getParent());
        StringBuilder report = new StringBuilder("# Staging read-only smoke evidence\n\n")
                .append("- Result: **").append(RESULTS.size() == 4 ? "Checks completed" : "Incomplete/failed")
                .append("** (CI job status remains authoritative)\n")
                .append("- Started (UTC): ").append(startedAt == null ? "configuration failed" : startedAt)
                .append("\n- Workflow run: ").append(workflowRun)
                .append("\n- Test repository revision: ")
                .append(System.getenv().getOrDefault("GITHUB_SHA", "local-unbound"))
                .append("\n- Backend/deployment revision: ")
                .append(policy == null ? "unavailable" : policy.deploymentRevision())
                .append("\n- Environment: staging\n\n| Check | Outcome |\n|---|---|\n");
        for (String check : new String[] {"gateway-readiness", "auth-service-readiness", "jwt-signing-key-set",
                "anonymous-protected-route"}) {
            report.append('|').append(check).append('|').append(RESULTS.getOrDefault(check, "Not run / blocked"))
                    .append("|\n");
        }
        report.append("\nThis is a bounded read-only smoke run, not full regression, performance, penetration, "
                + "or production certification. No target URL, request/response body, or credential is recorded.\n");
        Files.writeString(evidence, report.toString(), StandardCharsets.UTF_8);
    }

    private static String requiredField(JsonNode document, String field) {
        JsonNode value = document.path(field);
        assertTrue(value.isTextual() && !value.asText().isBlank(),
                "JWT key set must contain a non-empty " + field);
        return value.asText();
    }

    private static void awaitSuccess(String name, URI endpoint, StatusCheck accepted) {
        Instant deadline = Instant.now().plus(overallTimeout);
        String last = "no response";
        while (Instant.now().isBefore(deadline)) {
            int status;
            try {
                status = probe.getStatus(endpoint);
            } catch (RuntimeException exception) {
                last = exception.getClass().getSimpleName();
                pause();
                continue;
            }
            if (accepted.accepts(status)) {
                RESULTS.put(name, "Passed (HTTP " + status + ")");
                return;
            }
            last = "HTTP " + status;
            pause();
        }
        throw new AssertionError(name + " did not become ready within " + overallTimeout.toSeconds()
                + " seconds; last result: " + last);
    }

    private static void pause() {
        try {
            Thread.sleep(pollInterval.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Staging readiness wait interrupted", exception);
        }
    }

    private static Duration positiveSeconds(String name, long fallback) {
        String raw = System.getenv().getOrDefault(name, Long.toString(fallback));
        try {
            long seconds = Long.parseLong(raw);
            if (seconds <= 0 || seconds > 600) {
                throw new NumberFormatException("must be in range 1-600");
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be between 1 and 600 seconds", exception);
        }
    }

    @FunctionalInterface
    private interface StatusCheck {
        boolean accepts(int status);
    }
}
