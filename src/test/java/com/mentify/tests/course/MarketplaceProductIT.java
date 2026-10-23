package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import com.mentify.tests.environment.BackendReadinessClient;
import com.mentify.tests.environment.RequiredService;
import com.mentify.tests.environment.TestEnvironmentConfiguration;
import com.mentify.tests.material.MaterialClient;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MarketplaceProductIT extends CourseIntegrationTestSupport {

    private static MarketplaceClient marketplace;
    private static MaterialClient materials;

    @BeforeAll
    static void requireMarketplaceDependencies() {
        TestEnvironmentConfiguration environment = TestEnvironmentConfiguration.fromEnvironment();
        new BackendReadinessClient(environment).awaitService(RequiredService.MATERIAL_SERVICE);
        marketplace = new MarketplaceClient(apiClient);
        materials = new MaterialClient(apiClient);
    }

    @Test
    void anonymousUserCanListMarketplaceProducts() {
        Response response = marketplace.listPublic(null, null, null, 0, 20);
        assertEquals(200, response.statusCode(), response.asString());
        assertNotNull(response.jsonPath().get("data.content"), response.asString());
    }

    @Test
    void anonymousUserCanReadAConfiguredPublishedProduct() {
        String productId = System.getenv("TEST_MARKETPLACE_PRODUCT_ID");
        assumeTrue(productId != null && !productId.isBlank(),
                "Set TEST_MARKETPLACE_PRODUCT_ID to a published non-production product");
        Response response = marketplace.getPublic(productId);
        assertEquals(200, response.statusCode(), response.asString());
        assertEquals(productId, response.jsonPath().getString("data.id"));
    }

    @Test
    void administratorCanListMarketplaceProducts() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        try {
            Response response = marketplace.listAdmin(null, 0, 20, admin.accessToken());
            assertEquals(200, response.statusCode(), response.asString());
            assertNotNull(response.jsonPath().get("data.content"), response.asString());
        } finally {
            logout(admin);
        }
    }

    @Test
    void administratorCanCreateUpdatePublishAndArchiveMarketplaceProduct() throws Exception {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(
                        "TEST_MARKETPLACE_WRITE_ENABLED", "false")),
                "Set TEST_MARKETPLACE_WRITE_ENABLED=true only for approved non-production fixtures");
        AuthenticationSession admin = login(TestRole.ADMIN);
        String materialId = null;
        String productId = null;
        String title = "TEST marketplace " + UUID.randomUUID();
        Path upload = Files.createTempFile("mentify-marketplace-test-", ".txt");
        try {
            Files.writeString(upload, "Synthetic marketplace material " + UUID.randomUUID(),
                    StandardCharsets.UTF_8);
            Response uploaded = materials.uploadMarketplaceMaterial(title, "Synthetic test fixture",
                    upload.toFile(), admin.accessToken());
            assertEquals(201, uploaded.statusCode(), uploaded.asString());
            materialId = uploaded.jsonPath().getString("data.id");
            assertNotNull(materialId, uploaded.asString());

            Response created = marketplace.create(productRequest(materialId, title), admin.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            productId = created.jsonPath().getString("data.id");
            assertNotNull(productId, created.asString());
            assertEquals("DRAFT", created.jsonPath().getString("data.status"));

            Response listed = marketplace.listAdmin(title, 0, 20, admin.accessToken());
            assertEquals(200, listed.statusCode(), listed.asString());
            assertTrue(listed.jsonPath().getList("data.content.id", String.class).contains(productId), listed.asString());

            String updatedTitle = title + " updated";
            Map<String, Object> updatedRequest = productRequest(materialId, updatedTitle);
            Response updated = marketplace.update(productId, updatedRequest, admin.accessToken());
            assertEquals(200, updated.statusCode(), updated.asString());
            assertEquals(updatedTitle, updated.jsonPath().getString("data.title"));

            Response published = marketplace.changeStatus(productId, "PUBLISHED", admin.accessToken());
            assertEquals(200, published.statusCode(), published.asString());
            assertEquals("PUBLISHED", published.jsonPath().getString("data.status"));
            Response publicRead = marketplace.getPublic(productId);
            assertEquals(200, publicRead.statusCode(), publicRead.asString());
            assertEquals(productId, publicRead.jsonPath().getString("data.id"));

            Response archived = marketplace.changeStatus(productId, "ARCHIVED", admin.accessToken());
            assertEquals(200, archived.statusCode(), archived.asString());
            assertEquals("ARCHIVED", archived.jsonPath().getString("data.status"));
            productId = null;
        } finally {
            try {
                if (productId != null) {
                    marketplace.changeStatus(productId, "ARCHIVED", admin.accessToken());
                }
            } finally {
                try {
                    if (materialId != null) materials.delete(materialId, admin.accessToken());
                } finally {
                    Files.deleteIfExists(upload);
                    logout(admin);
                }
            }
        }
    }

    private static Map<String, Object> productRequest(String materialId, String title) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("materialId", materialId);
        request.put("title", title);
        request.put("shortDescription", "Synthetic integration fixture");
        request.put("description", "Created only by the gated marketplace integration test");
        request.put("category", "OTHER");
        request.put("price", 0);
        request.put("free", true);
        request.put("discountPercent", 0);
        request.put("currency", "LKR");
        request.put("status", "DRAFT");
        return request;
    }
}
