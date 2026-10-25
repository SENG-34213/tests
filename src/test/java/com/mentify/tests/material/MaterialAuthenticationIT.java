package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MaterialAuthenticationIT extends MaterialIntegrationTestSupport {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";
    private static final byte[] SYNTHETIC_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/jWQAAAAASUVORK5CYII=");

    @Test
    void rejectsProtectedMaterialRequestsWithoutAuthentication() {
        assertEquals(401, materialClient.uploadWithoutFile(Map.of(), null).statusCode());
        assertEquals(401, materialClient.list(Map.of(), null).statusCode());
        assertEquals(401, materialClient.get(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.download(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.update(UNKNOWN_ID, Map.of(), null).statusCode());
        assertEquals(401, materialClient.archive(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.delete(UNKNOWN_ID, null).statusCode());
        assertEquals(401, materialClient.audit(Map.of("dryRun", true), null).statusCode());
        assertEquals(401, materialClient.cleanup(Map.of("dryRun", true), null).statusCode());
    }

    @Test
    void studentCannotManageMaterialsOrRunMaintenance() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(403, materialClient.update(UNKNOWN_ID, Map.of("title", "blocked"),
                    student.accessToken()).statusCode());
            assertEquals(403, materialClient.archive(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, materialClient.delete(UNKNOWN_ID, student.accessToken()).statusCode());
            assertEquals(403, materialClient.audit(Map.of("dryRun", true),
                    student.accessToken()).statusCode());
            assertEquals(403, materialClient.cleanup(Map.of("dryRun", true),
                    student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void teacherCannotRunMaintenance() {
        AuthenticationSession teacher = login(TestRole.TEACHER);
        try {
            assertEquals(403, materialClient.audit(Map.of("dryRun", true),
                    teacher.accessToken()).statusCode());
            assertEquals(403, materialClient.cleanup(Map.of("dryRun", true),
                    teacher.accessToken()).statusCode());
        } finally {
            logout(teacher);
        }
    }

    @Test
    void profileImageUploadRequiresAnImagePart() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            assertEquals(400, materialClient.uploadProfileImageWithoutFile(student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void studentCannotDownloadAnotherUsersProfileImage() {
        AuthenticationSession student = login(TestRole.STUDENT);
        try {
            String otherUsersKey = "materials/profile-images/00000000-0000-0000-0000-000000000001/image.png";
            assertEquals(403, materialClient.downloadProfileImage(otherUsersKey, student.accessToken()).statusCode());
        } finally {
            logout(student);
        }
    }

    @Test
    void studentCanUploadAndDownloadOwnProfileImageFromLocalTestStorage() throws IOException {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_PROFILE_IMAGE_WRITE_ENABLED", "false")),
                "Set TEST_PROFILE_IMAGE_WRITE_ENABLED=true for local profile-image integration");
        LocalProfileImageStorageCleanup storageCleanup = LocalProfileImageStorageCleanup.fromEnvironment();
        Path image = Files.createTempFile("mentify-profile-image-test-", ".png");
        Files.write(image, SYNTHETIC_PNG);
        AuthenticationSession student = null;
        String storageKey = null;
        try {
            student = login(TestRole.STUDENT);
            Response uploaded = materialClient.uploadProfileImage(image.toFile(), student.accessToken());
            assertEquals(201, uploaded.statusCode(), uploaded.asString());
            storageKey = uploaded.jsonPath().getString("data.storageKey");
            assertNotNull(storageKey, uploaded.asString());
            assertEquals("image/png", uploaded.jsonPath().getString("data.contentType"));

            Response downloaded = materialClient.downloadProfileImage(storageKey, student.accessToken());
            assertEquals(200, downloaded.statusCode(), downloaded.asString());
            assertTrue(downloaded.getContentType().startsWith("image/png"), downloaded.getContentType());
            assertArrayEquals(SYNTHETIC_PNG, downloaded.asByteArray());
        } finally {
            try {
                if (storageKey != null) {
                    storageCleanup.deleteCreatedProfileImage(storageKey);
                }
            } finally {
                try {
                    logout(student);
                } finally {
                    try {
                        Files.deleteIfExists(image);
                    } finally {
                        storageCleanup.close();
                    }
                }
            }
        }
    }
}
