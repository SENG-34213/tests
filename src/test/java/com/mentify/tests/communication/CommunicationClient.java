package com.mentify.tests.communication;

import com.mentify.tests.api.ApiClient;
import com.mentify.tests.api.HttpMethod;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class CommunicationClient {

    private static final String PATH = "/api/communication";
    private final ApiClient apiClient;

    public CommunicationClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response me(String token) {
        return apiClient.get(PATH + "/me", token, Map.of(), Map.of());
    }

    public Response createGroup(Object request, String token) {
        return apiClient.post(PATH + "/groups", request, token);
    }

    public Response listGroups(String token) {
        return apiClient.get(PATH + "/groups", token, Map.of(), Map.of());
    }

    public Response getGroup(String groupId, String token) {
        return apiClient.get(PATH + "/groups/" + id(groupId, "groupId"), token, Map.of(), Map.of());
    }

    public Response listMembers(String groupId, String token) {
        return apiClient.get(PATH + "/groups/" + id(groupId, "groupId") + "/members",
                token, Map.of(), Map.of());
    }

    public Response addStudent(String groupId, Object request, String token) {
        return apiClient.post(PATH + "/groups/" + id(groupId, "groupId") + "/students", request, token);
    }

    public Response addStudentToCourse(String courseId, Object request, String token) {
        return apiClient.post(PATH + "/groups/courses/" + id(courseId, "courseId") + "/students", request, token);
    }

    public Response archive(String groupId, String token) {
        return apiClient.post(PATH + "/groups/" + id(groupId, "groupId") + "/archive", null, token);
    }

    public Response unarchive(String groupId, String token) {
        return apiClient.post(PATH + "/groups/" + id(groupId, "groupId") + "/unarchive", null, token);
    }

    public Response sendMessage(String groupId, Object request, String token) {
        return apiClient.post(messagePath(groupId), request, token);
    }

    public Response messageHistory(String groupId, int page, int size, String token) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("page must be non-negative and size must be positive");
        }
        return apiClient.get(messagePath(groupId), token, Map.of("page", page, "size", size), Map.of());
    }

    public Response updateMessage(String groupId, String messageId, Object request, String token) {
        return apiClient.patch(messagePath(groupId) + "/" + id(messageId, "messageId"), request, token);
    }

    public Response deleteMessage(String groupId, String messageId, String scope, String token) {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("scope must not be blank");
        }
        return apiClient.requestResponse(HttpMethod.DELETE,
                messagePath(groupId) + "/" + id(messageId, "messageId"),
                null, token, Map.of("scope", scope), Map.of());
    }

    public Response getSettings(String token) {
        return apiClient.get(PATH + "/settings", token, Map.of(), Map.of());
    }

    public Response updateSettings(Object request, String token) {
        return apiClient.patch(PATH + "/settings", request, token);
    }

    private static String messagePath(String groupId) {
        return PATH + "/groups/" + id(groupId, "groupId") + "/messages";
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
