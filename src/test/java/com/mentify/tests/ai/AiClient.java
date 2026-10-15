package com.mentify.tests.ai;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class AiClient {

    private static final String PATH = "/api/ai";
    private final ApiClient apiClient;

    public AiClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response health() {
        return apiClient.get(PATH + "/health");
    }

    public Response me(String token) {
        return apiClient.get(PATH + "/me", token, Map.of(), Map.of());
    }

    public Response createConversation(Object request, String token) {
        return apiClient.post(PATH + "/conversations", request, token);
    }

    public Response conversations(String token) {
        return apiClient.get(PATH + "/conversations", token, Map.of(), Map.of());
    }

    public Response conversation(String conversationId, String token) {
        return apiClient.get(PATH + "/conversations/" + id(conversationId, "conversationId"),
                token, Map.of(), Map.of());
    }

    public Response messages(String conversationId, String token) {
        return apiClient.get(PATH + "/conversations/" + id(conversationId, "conversationId") + "/messages",
                token, Map.of(), Map.of());
    }

    public Response updateConversation(String conversationId, Object request, String token) {
        return apiClient.put(PATH + "/conversations/" + id(conversationId, "conversationId"), request, token);
    }

    public Response deleteConversation(String conversationId, String token) {
        return apiClient.delete(PATH + "/conversations/" + id(conversationId, "conversationId"), token);
    }

    public Response chat(Object request, String token) {
        return apiClient.post(PATH + "/chat", request, token);
    }

    public Response generate(Object request, String token) {
        return apiClient.post(PATH + "/generate", request, token);
    }

    public Response summarizeLesson(String lessonId, String token) {
        return apiClient.post(PATH + "/lessons/" + id(lessonId, "lessonId") + "/summarize", null, token);
    }

    public Response summarizeMaterial(String materialId, String token) {
        return apiClient.post(PATH + "/lessons/materials/" + id(materialId, "materialId") + "/summarize",
                null, token);
    }

    public Response generateQuiz(Map<String, ?> fields, File file, String token) {
        return apiClient.postMultipart(PATH + "/quizzes/generate", fields, file, token);
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
