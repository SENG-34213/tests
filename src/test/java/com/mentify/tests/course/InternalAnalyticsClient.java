package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Objects;

public class InternalAnalyticsClient {

    private final ApiClient apiClient;

    public InternalAnalyticsClient(ApiClient api) {
        this.apiClient = Objects.requireNonNull(api, "apiClient must not be null");
    }

    public Response assignmentOverview(String token) {
        return apiClient.get("/assignment-service/api/v1/internal/analytics/assignments/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response assignmentCourseSummary(String courseId, String token) {
        return apiClient.get("/assignment-service/api/v1/internal/analytics/assignments/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response quizContractOverview(String token) {
        return apiClient.get("/api/v1/internal/analytics/quizzes/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response quizContractCourseSummary(String courseId, String token) {
        return apiClient.get("/api/v1/internal/analytics/quizzes/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response quizOverview(String token) {
        return apiClient.get("/assignment-service/api/internal/quizzes/analytics/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response quizTodayPerformance(String token) {
        return apiClient.get("/assignment-service/api/internal/quizzes/analytics/today/performance", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response quizCourseSummary(String courseId, String token) {
        return apiClient.get("/assignment-service/api/internal/quizzes/analytics/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response attendanceOverview(String token) {
        return apiClient.get("/attendance-service/api/v1/internal/analytics/attendance/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response attendanceCourseSummary(String courseId, String token) {
        return apiClient.get("/attendance-service/api/v1/internal/analytics/attendance/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response courseOverview(String token) {
        return apiClient.get("/course-service/api/v1/internal/analytics/courses/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response enrollmentOverview(String token) {
        return apiClient.get("/entrollment-service/api/v1/internal/analytics/enrollments/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response enrollmentCourseSummary(String courseId, String token) {
        return apiClient.get("/entrollment-service/api/v1/internal/analytics/enrollments/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response examOverview(String token) {
        return apiClient.get("/exam-service/api/v1/internal/analytics/exams/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response examCourseSummary(String courseId, String token) {
        return apiClient.get("/exam-service/api/v1/internal/analytics/exams/courses/" + courseId,
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response paymentOverview(String token) {
        return apiClient.get("/payment-service/api/v1/internal/analytics/payments/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response userRegistrationOverview(String token) {
        return apiClient.get("/api/internal/users/analytics/registration-overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response userOverview(String token) {
        return apiClient.get("/api/internal/users/analytics/users/overview", token, java.util.Map.of(), java.util.Map.of());
    }

    public Response userRegistrationOverviewDiscovery(String token) {
        return apiClient.get("/user-service/api/v1/internal/analytics/registration-overview",
                token, java.util.Map.of(), java.util.Map.of());
    }

    public Response userOverviewDiscovery(String token) {
        return apiClient.get("/user-service/api/v1/internal/analytics/users/overview",
                token, java.util.Map.of(), java.util.Map.of());
    }
}
