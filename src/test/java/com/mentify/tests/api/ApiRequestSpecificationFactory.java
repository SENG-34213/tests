package com.mentify.tests.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

public class ApiRequestSpecificationFactory {

    private final ApiClientConfiguration configuration;

    public ApiRequestSpecificationFactory(ApiClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public RequestSpecification create() {
        return create(null, Map.of());
    }

    public RequestSpecification create(String bearerToken, Map<String, String> headers) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(configuration.baseUri().toString())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", (int) configuration.connectTimeout().toMillis())
                                .setParam("http.socket.timeout", (int) configuration.readTimeout().toMillis())));

        headers.forEach(builder::addHeader);
        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + bearerToken);
        }
        return builder.build();
    }
}
