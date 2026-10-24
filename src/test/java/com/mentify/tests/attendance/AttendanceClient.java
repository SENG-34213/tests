package com.mentify.tests.attendance;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class AttendanceClient {

    private static final String PATH = "/api/v1/attendance";
    private final ApiClient apiClient;

    public AttendanceClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response createSession(Object request, String token) {
        return apiClient.post(PATH + "/sessions", request, token);
    }

    public Response listSessions(String courseId, String token) {
        return apiClient.get(PATH + "/courses/" + id(courseId, "courseId") + "/sessions",
                token, Map.of(), Map.of());
    }

    public Response getSession(String sessionId, String token) {
        return apiClient.get(PATH + "/sessions/" + id(sessionId, "sessionId"),
                token, Map.of(), Map.of());
    }

    public Response markAttendance(String sessionId, Object request, String token) {
        return apiClient.put(PATH + "/sessions/" + id(sessionId, "sessionId") + "/records", request, token);
    }

    public Response complete(String sessionId, String token) {
        return apiClient.post(PATH + "/sessions/" + id(sessionId, "sessionId") + "/complete", null, token);
    }

    public Response cancel(String sessionId, String token) {
        return apiClient.post(PATH + "/sessions/" + id(sessionId, "sessionId") + "/cancel", null, token);
    }

    public Response deleteSession(String sessionId, String token) {
        return apiClient.delete(PATH + "/sessions/" + id(sessionId, "sessionId"), token);
    }

    public Response mySummary(String courseId, String token) {
        return apiClient.get(PATH + "/courses/" + id(courseId, "courseId") + "/me",
                token, Map.of(), Map.of());
    }

    public Response studentSummary(String studentId, String courseId, String token) {
        return apiClient.get(PATH + "/students/" + id(studentId, "studentId")
                        + "/courses/" + id(courseId, "courseId"), token, Map.of(), Map.of());
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
