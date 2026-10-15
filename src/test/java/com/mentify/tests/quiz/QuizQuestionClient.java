package com.mentify.tests.quiz;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Objects;

public class QuizQuestionClient {

    private final ApiClient apiClient;

    public QuizQuestionClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(String quizId, Object request, String token) {
        return apiClient.post(path(quizId), request, token);
    }

    public Response update(String quizId, String questionId, Object request, String token) {
        return apiClient.put(path(quizId) + "/" + id(questionId, "questionId"), request, token);
    }

    public Response delete(String quizId, String questionId, String token) {
        return apiClient.delete(path(quizId) + "/" + id(questionId, "questionId"), token);
    }

    private static String path(String quizId) {
        return "/api/quizzes/" + id(quizId, "quizId") + "/questions";
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
