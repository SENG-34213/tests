package com.mentify.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class ApiClient {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    private final ApiClientConfiguration configuration;
    private final ApiRequestSpecificationFactory specificationFactory;
    private final String defaultBearerToken;

    public ApiClient(ApiClientConfiguration configuration) {
        this(configuration, null);
    }

    private ApiClient(ApiClientConfiguration configuration, String defaultBearerToken) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.specificationFactory = new ApiRequestSpecificationFactory(configuration);
        this.defaultBearerToken = defaultBearerToken;
    }

    public ApiClient withBearerToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("bearerToken must not be blank");
        }
        return new ApiClient(configuration, bearerToken);
    }

    public Response get(String path) {
        return execute(HttpMethod.GET, path, null, null, Map.of(), Map.of());
    }

    public Response get(String path, String bearerToken, Map<String, ?> queryParameters,
                        Map<String, String> headers) {
        return execute(HttpMethod.GET, path, null, bearerToken, queryParameters, headers);
    }

    public Response post(String path, Object body, String bearerToken) {
        return execute(HttpMethod.POST, path, body, bearerToken, Map.of(), Map.of());
    }

    public Response postWithHeaders(String path, Object body, String bearerToken, Map<String, String> headers) {
        return execute(HttpMethod.POST, path, body, bearerToken, Map.of(), headers);
    }

    public Response postMultipart(String path, Map<String, ?> fields, File file, String bearerToken) {
        return postMultipart(path, fields, file, bearerToken, null, null);
    }

    public Response postMultipart(String path, Map<String, ?> fields, File file, String bearerToken,
                                  String jsonPartName, Object jsonPart) {
        validatePath(path);
        Objects.requireNonNull(fields, "fields must not be null");
        String effectiveBearerToken = bearerToken == null ? defaultBearerToken : bearerToken;
        RequestSpecification request = RestAssured.given(
                        specificationFactory.create(effectiveBearerToken, Map.of()));
        request.contentType("multipart/form-data");
        if (file != null) {
            request.multiPart("file", file, fileContentType(file));
        }
        if (jsonPartName != null) {
            request.multiPart(jsonPartName, Objects.requireNonNull(jsonPart, "jsonPart must not be null"),
                    "application/json");
        }
        fields.forEach((name, value) -> request.multiPart(name, String.valueOf(value)));
        if (configuration.loggingEnabled()) {
            LOGGER.info(() -> "POST " + path + " (multipart fields, file, and credentials are omitted)");
        }
        Response response = request.when().post(path);
        if (configuration.loggingEnabled()) {
            LOGGER.info(() -> "POST " + path + " -> " + response.statusCode());
        }
        return response;
    }

    public Response put(String path, Object body, String bearerToken) {
        return execute(HttpMethod.PUT, path, body, bearerToken, Map.of(), Map.of());
    }

    public Response putMultipart(String path, Map<String, ?> fields, File file, String bearerToken,
                                 String jsonPartName, Object jsonPart) {
        validatePath(path);
        Objects.requireNonNull(fields, "fields must not be null");
        String effectiveBearerToken = bearerToken == null ? defaultBearerToken : bearerToken;
        RequestSpecification request = RestAssured.given(
                specificationFactory.create(effectiveBearerToken, Map.of()));
        request.contentType("multipart/form-data");
        if (file != null) request.multiPart("file", file, fileContentType(file));
        if (jsonPartName != null) {
            request.multiPart(jsonPartName, Objects.requireNonNull(jsonPart, "jsonPart must not be null"),
                    "application/json");
        }
        fields.forEach((name, value) -> request.multiPart(name, String.valueOf(value)));
        if (configuration.loggingEnabled()) {
            LOGGER.info(() -> "PUT " + path + " (multipart fields, file, JSON part, and credentials are omitted)");
        }
        Response response = request.when().put(path);
        if (configuration.loggingEnabled()) LOGGER.info(() -> "PUT " + path + " -> " + response.statusCode());
        return response;
    }

    public Response patch(String path, Object body, String bearerToken) {
        return execute(HttpMethod.PATCH, path, body, bearerToken, Map.of(), Map.of());
    }

    public Response patch(String path, Object body, String bearerToken,
                          Map<String, ?> queryParameters, Map<String, String> headers) {
        return execute(HttpMethod.PATCH, path, body, bearerToken, queryParameters, headers);
    }

    public Response delete(String path, String bearerToken) {
        return execute(HttpMethod.DELETE, path, null, bearerToken, Map.of(), Map.of());
    }

    public ApiResponse request(HttpMethod method, String path, Object body, String bearerToken,
                               Map<String, ?> queryParameters, Map<String, String> headers) {
        return ApiResponse.from(execute(method, path, body, bearerToken, queryParameters, headers));
    }

    public Response requestResponse(HttpMethod method, String path, Object body, String bearerToken,
                                    Map<String, ?> queryParameters, Map<String, String> headers) {
        return execute(method, path, body, bearerToken, queryParameters, headers);
    }

    private Response execute(HttpMethod method, String path, Object body, String bearerToken,
                             Map<String, ?> queryParameters, Map<String, String> headers) {
        validatePath(path);

        String effectiveBearerToken = bearerToken == null ? defaultBearerToken : bearerToken;
        RequestSpecification request = RestAssured.given(specificationFactory.create(effectiveBearerToken, headers))
                .queryParams(queryParameters);
        if (body != null) {
            request.body(body);
        }

        if (configuration.loggingEnabled()) {
            LOGGER.info(() -> method + " " + path + " (request/response bodies and credentials are omitted)");
        }

        Response response = switch (method) {
            case GET -> request.when().get(path);
            case POST -> request.when().post(path);
            case PUT -> request.when().put(path);
            case PATCH -> request.when().patch(path);
            case DELETE -> request.when().delete(path);
        };

        if (configuration.loggingEnabled()) {
            LOGGER.info(() -> method + " " + path + " -> " + response.statusCode());
        }
        return response;
    }

    private static void validatePath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("API path must be non-empty and start with '/'");
        }
    }

    private static String fileContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return contentType == null ? "application/octet-stream" : contentType;
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }
}
