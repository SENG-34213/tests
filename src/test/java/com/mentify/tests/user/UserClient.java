package com.mentify.tests.user;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class UserClient {

    private static final String AUTH = "/api/v1/auth";
    private final ApiClient apiClient;

    public UserClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response publicTest() { return apiClient.get(AUTH + "/public-test"); }

    public Response protectedTest(String token) {
        return apiClient.get(AUTH + "/protected-test", token, Map.of(), Map.of());
    }

    public Response adminTest(String token) {
        return apiClient.get(AUTH + "/admin-test", token, Map.of(), Map.of());
    }

    public Response studentTest(String token) {
        return apiClient.get(AUTH + "/student-test", token, Map.of(), Map.of());
    }

    public Response login(Object request) { return apiClient.post(AUTH + "/login", request, null); }

    public Response refresh(Object request) { return apiClient.post(AUTH + "/refresh", request, null); }

    public Response logout(Object request) { return apiClient.post(AUTH + "/logout", request, null); }

    public Response forgotPassword(Object request) { return apiClient.post(AUTH + "/forgot-password", request, null); }

    public Response currentUser(String token) {
        return apiClient.get("/api/v1/users/me", token, Map.of(), Map.of());
    }

    public Response usersByRole(String role, String token) {
        return apiClient.get("/api/v1/users", token, Map.of("role", role), Map.of());
    }

    public Response registerUser(Object request, String token) {
        return apiClient.post("/api/v1/users/register", request, token);
    }

    public Response registerAdmin(Object request, String token) {
        return apiClient.post("/api/v1/users/admins/register", request, token);
    }

    public Response resendInvitation(String userId, String token) {
        return apiClient.post("/api/v1/users/" + id(userId) + "/resend-invitation", null, token);
    }

    public Response updateStatus(String userId, Object request, String token) {
        return apiClient.patch("/api/v1/users/" + id(userId) + "/status", request, token);
    }

    public Response studentExists(String studentId, String token) {
        return apiClient.get("/api/v1/users/students/" + id(studentId) + "/exists", token, Map.of(), Map.of());
    }

    public Response teacherExists(String teacherId, String token) {
        return apiClient.get("/api/v1/users/teachers/" + id(teacherId) + "/exists", token, Map.of(), Map.of());
    }

    public Response registerStudent(Object request, String token) {
        return apiClient.post("/api/students/register", request, token);
    }

    public Response listStudents(int page, int size, String token) {
        return apiClient.get("/api/students/all-students", token, Map.of("page", page, "size", size), Map.of());
    }

    public Response listTeachers(int page, int size, String token) {
        return apiClient.get("/api/teachers/all-teachers", token, Map.of("page", page, "size", size), Map.of());
    }

    public Response adminUsers(String role, String token) {
        return apiClient.get("/api/v1/admin/users", token, Map.of("role", role), Map.of());
    }

    public Response analytics(String token) {
        return apiClient.get("/api/internal/users/analytics/registration-overview", token, Map.of(), Map.of());
    }

    private static String id(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        return value;
    }
}
