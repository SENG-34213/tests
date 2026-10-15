package com.mentify.tests.api;

import io.restassured.response.Response;
import java.util.Map;

public record ApiResponse(int statusCode, String body, Map<String, String> headers, long timeInMillis) {

    public static ApiResponse from(Response response) {
        Map<String, String> responseHeaders = response.headers().asList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        header -> header.getName(),
                        header -> header.getValue(),
                        (first, second) -> first));
        return new ApiResponse(response.statusCode(), response.asString(), responseHeaders, response.time());
    }
}
