package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import java.net.URI;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

final class LocalProfileImageStorageCleanup implements AutoCloseable {

    private final S3Client client;
    private final String bucket;

    private LocalProfileImageStorageCleanup(S3Client client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    static LocalProfileImageStorageCleanup fromEnvironment() {
        URI gateway = TestEnvironmentConfiguration.fromEnvironment().serviceUrls()
                .get(RequiredService.API_GATEWAY);
        String gatewayHost = gateway.getHost();
        assumeTrue(gatewayHost != null && (gatewayHost.equalsIgnoreCase("localhost")
                        || gatewayHost.equals("127.0.0.1") || gatewayHost.equals("::1")),
                "Profile-image writes are restricted to a local API Gateway");
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_PROFILE_IMAGE_STORAGE_TARGET_CONFIRMED", "false")),
                "Confirm Material Service uses the same local test endpoint and bucket");
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_PROFILE_IMAGE_STORAGE_CLEANUP_ENABLED", "false")),
                "Enable exact-key cleanup only for the local profile-image test bucket");
        URI endpoint = URI.create(required("MATERIAL_S3_ENDPOINT"));
        String host = endpoint.getHost();
        assumeTrue(("http".equalsIgnoreCase(endpoint.getScheme())
                        || "https".equalsIgnoreCase(endpoint.getScheme()))
                        && host != null && (host.equalsIgnoreCase("localhost")
                        || host.equals("127.0.0.1") || host.equals("::1"))
                        && endpoint.getUserInfo() == null && endpoint.getQuery() == null
                        && endpoint.getFragment() == null,
                "Profile-image cleanup is restricted to a local S3-compatible endpoint");

        String bucket = required("MATERIAL_S3_BUCKET");
        String approvedBucket = required("TEST_PROFILE_IMAGE_TEST_BUCKET");
        String normalizedBucket = bucket.toLowerCase(Locale.ROOT);
        assumeTrue(bucket.equals(approvedBucket)
                        && (normalizedBucket.contains("test") || normalizedBucket.contains("local")),
                "MATERIAL_S3_BUCKET must equal a bucket name containing test or local");
        String accessKey = required("AWS_ACCESS_KEY_ID");
        String secretKey = required("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv().getOrDefault("AWS_REGION", "ap-south-1");

        S3Client client = S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        return new LocalProfileImageStorageCleanup(client, bucket);
    }

    void deleteCreatedProfileImage(String storageKey) {
        Assertions.assertNotNull(storageKey);
        assertTrue(storageKey.startsWith("materials/profile-images/"),
                "Cleanup accepts only a profile-image object key returned by the upload API");
        assertTrue(!storageKey.contains("..") && !storageKey.contains("\\"),
                "Profile-image key must not contain path traversal segments");
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
    }

    @Override
    public void close() {
        client.close();
    }

    private static String required(String name) {
        String value = System.getenv(name);
        assumeTrue(value != null && !value.isBlank(), "Set " + name + " for the local profile-image test");
        return value.trim();
    }
}
