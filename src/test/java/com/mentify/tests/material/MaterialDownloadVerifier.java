package com.mentify.tests.material;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class MaterialDownloadVerifier {

    private MaterialDownloadVerifier() {
    }

    public static void verifyIfEnabled(String downloadUrl, Path uploadedFile) throws Exception {
        if (!Boolean.parseBoolean(System.getenv().getOrDefault(
                "TEST_MATERIAL_DOWNLOAD_BYTES_ENABLED", "false"))) {
            return;
        }

        String allowedHost = System.getenv("TEST_MATERIAL_DOWNLOAD_ALLOWED_HOST");
        assertNotNull(allowedHost,
                "Set TEST_MATERIAL_DOWNLOAD_ALLOWED_HOST when enabling signed-download byte verification");
        boolean allowLocalHttp = Boolean.parseBoolean(System.getenv().getOrDefault(
                "TEST_MATERIAL_DOWNLOAD_LOCAL_HTTP_ENABLED", "false"));
        java.net.URI uri = MaterialDownloadUrlPolicy.validate(downloadUrl, allowedHost, allowLocalHttp);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<byte[]> downloaded = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, downloaded.statusCode(), "Presigned object-storage GET must succeed");
        assertArrayEquals(Files.readAllBytes(uploadedFile), downloaded.body(),
                "Downloaded bytes must exactly match the uploaded synthetic file");
    }
}
