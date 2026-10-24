package com.mentify.tests.staging;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Validates the opt-in target and operator confirmation before any staging request is sent. */
public final class StagingTargetPolicy {

    public static final String REQUIRED_CONFIRMATION = "RUN STAGING READ-ONLY SMOKE";

    private final URI gatewayBase;
    private final URI authServiceBase;
    private final String deploymentRevision;

    private StagingTargetPolicy(URI gatewayBase, URI authServiceBase,
                                String allowedHostSuffix, String deploymentRevision) {
        this.gatewayBase = gatewayBase;
        this.authServiceBase = authServiceBase;
        this.deploymentRevision = deploymentRevision;
    }

    public static StagingTargetPolicy fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static StagingTargetPolicy fromEnvironment(Map<String, String> environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        if (!"staging".equals(environment.get("STAGING_ENVIRONMENT"))) {
            throw new IllegalArgumentException("STAGING_ENVIRONMENT must be exactly 'staging'");
        }
        if (!REQUIRED_CONFIRMATION.equals(environment.get("STAGING_CONFIRMATION"))) {
            throw new IllegalArgumentException("Explicit staging confirmation is required");
        }

        String suffix = required(environment, "STAGING_ALLOWED_HOST_SUFFIX").toLowerCase(Locale.ROOT);
        if (!suffix.matches("(?i)(?=.{3,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}")) {
            throw new IllegalArgumentException("STAGING_ALLOWED_HOST_SUFFIX must be a DNS suffix, not a URL or IP");
        }
        URI gateway = validatedHttpsUrl(required(environment, "STAGING_BASE_URL"), suffix, "STAGING_BASE_URL");
        URI authService = validatedHttpsUrl(
                required(environment, "STAGING_AUTH_SERVICE_BASE_URL"), suffix, "STAGING_AUTH_SERVICE_BASE_URL");
        String revision = required(environment, "STAGING_DEPLOYMENT_REVISION");
        if (!revision.matches("(?i)[0-9a-f]{7,40}")) {
            throw new IllegalArgumentException("STAGING_DEPLOYMENT_REVISION must be a 7-40 character Git SHA");
        }
        return new StagingTargetPolicy(gateway, authService, suffix, revision.toLowerCase(Locale.ROOT));
    }

    URI gatewayBase() {
        return gatewayBase;
    }

    URI authServiceBase() {
        return authServiceBase;
    }

    URI jwtKeySet() {
        return authServiceBase.resolve("api/v1/auth/.well-known/jwks.json");
    }

    URI authServiceHealth() {
        return authServiceBase.resolve("actuator/health");
    }

    String deploymentRevision() {
        return deploymentRevision;
    }

    private static URI validatedHttpsUrl(String raw, String allowedSuffix, String variable) {
        try {
            URI uri = URI.create(raw.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)) {
                throw new IllegalArgumentException("must be an HTTPS origin without credentials, query, or fragment");
            }
            String normalizedHost = host.toLowerCase(Locale.ROOT);
            if (!(normalizedHost.equals(allowedSuffix) || normalizedHost.endsWith("." + allowedSuffix))) {
                throw new IllegalArgumentException("host is outside STAGING_ALLOWED_HOST_SUFFIX");
            }
            if (uri.getPath() != null && uri.getPath().contains("..")) {
                throw new IllegalArgumentException("path traversal is not allowed");
            }
            String normalized = uri.toString();
            return URI.create(normalized.endsWith("/") ? normalized : normalized + "/");
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + variable + ": " + exception.getMessage(), exception);
        }
    }

    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
