package com.mentify.tests.quiz;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Objects;

public class QuizAttemptClient {

    private final ApiClient apiClient;

    public QuizAttemptClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response start(String quizId, String token) {
        return apiClient.post("/api/quizzes/" + id(quizId, "quizId") + "/attempts", null, token);
    }

    public Response saveAnswer(String attemptId, Object request, String token) {
        return apiClient.put("/api/quiz-attempts/" + id(attemptId, "attemptId") + "/answers", request, token);
    }

    public Response submit(String attemptId, String token) {
        return apiClient.post("/api/quiz-attempts/" + id(attemptId, "attemptId") + "/submit", null, token);
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
