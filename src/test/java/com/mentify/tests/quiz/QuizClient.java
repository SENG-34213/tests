package com.mentify.tests.quiz;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class QuizClient {

    private static final String PATH = "/api/quizzes";
    private final ApiClient apiClient;

    public QuizClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(Object request, String token) {
        return apiClient.post(PATH, request, token);
    }

    public Response update(String quizId, Object request, String token) {
        return apiClient.put(quizPath(quizId), request, token);
    }

    public Response getTeacher(String quizId, String token) {
        return apiClient.get(quizPath(quizId), token, Map.of(), Map.of());
    }

    public Response listTeacher(String token) {
        return apiClient.get(PATH + "/teacher", token, Map.of(), Map.of());
    }

    public Response delete(String quizId, String token) {
        return apiClient.delete(quizPath(quizId), token);
    }

    public Response publish(String quizId, String token) {
        return apiClient.post(quizPath(quizId) + "/publish", null, token);
    }

    public Response listForCourse(String courseId, String token) {
        return apiClient.get("/api/courses/" + requiredId(courseId, "courseId") + "/quizzes",
                token, Map.of(), Map.of());
    }

    public Response getStudent(String quizId, String token) {
        return apiClient.get(quizPath(quizId) + "/student", token, Map.of(), Map.of());
    }

    private static String quizPath(String quizId) {
        return PATH + "/" + requiredId(quizId, "quizId");
    }

    private static String requiredId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
