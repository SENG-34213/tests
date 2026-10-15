package com.mentify.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class ApiClient {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    private final ApiClientConfiguration configuration;
    private final ApiRequestSpecificationFactory specificationFactory;

    public ApiClient(ApiClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.specificationFactory = new ApiRequestSpecificationFactory(configuration);
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

    public Response put(String path, Object body, String bearerToken) {
        return execute(HttpMethod.PUT, path, body, bearerToken, Map.of(), Map.of());
    }

    public Response patch(String path, Object body, String bearerToken) {
        return execute(HttpMethod.PATCH, path, body, bearerToken, Map.of(), Map.of());
    }

    public Response delete(String path, String bearerToken) {
        return execute(HttpMethod.DELETE, path, null, bearerToken, Map.of(), Map.of());
    }

    public ApiResponse request(HttpMethod method, String path, Object body, String bearerToken,
                               Map<String, ?> queryParameters, Map<String, String> headers) {
        return ApiResponse.from(execute(method, path, body, bearerToken, queryParameters, headers));
    }

    private Response execute(HttpMethod method, String path, Object body, String bearerToken,
                             Map<String, ?> queryParameters, Map<String, String> headers) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("API path must be non-empty and start with '/'");
        }

        RequestSpecification request = RestAssured.given(specificationFactory.create(bearerToken, headers))
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
}
