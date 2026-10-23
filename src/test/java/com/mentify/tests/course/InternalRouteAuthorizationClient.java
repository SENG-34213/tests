package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

/** Requests internal routes without credentials; must never perform a mutation. */
public class InternalRouteAuthorizationClient {

    private final ApiClient apiClient;

    public InternalRouteAuthorizationClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response assignmentReconcile(String id) {
        return apiClient.post("/assignment-service/api/v1/internal/calendar-reconciliation/assignments/" + id, null, null);
    }

    public Response quizReconcile(String id) {
        return apiClient.post("/assignment-service/api/v1/internal/calendar-reconciliation/quizzes/" + id, null, null);
    }

    public Response attendanceReconcile(String id) {
        return apiClient.post("/attendance-service/api/v1/internal/calendar-reconciliation/class-sessions/" + id, null, null);
    }

    public Response authProvision() {
        return apiClient.post("/api/v1/auth/internal/users", Map.of(), null);
    }

    public Response authChangePassword(String id) {
        return apiClient.post("/api/v1/auth/internal/users/" + id + "/password", Map.of(), null);
    }

    public Response authDelete(String id) {
        return apiClient.delete("/api/v1/auth/internal/users/" + id, null);
    }

    public Response calendarUpsertAssignment() {
        return apiClient.post("/calender-service/api/v1/internal/calendar/assignment-projections", Map.of(), null);
    }

    public Response calendarCancelAssignment(String id) {
        return apiClient.post("/calender-service/api/v1/internal/calendar/assignment-projections/" + id + "/cancel", Map.of(), null);
    }

    public Response calendarUpsertQuiz() {
        return apiClient.post("/calender-service/api/v1/internal/calendar/quiz-projections", Map.of(), null);
    }

    public Response calendarCancelQuiz(String id) {
        return apiClient.post("/calender-service/api/v1/internal/calendar/quiz-projections/" + id + "/cancel", Map.of(), null);
    }

    public Response calendarUpsertClass() {
        return apiClient.post("/calender-service/api/v1/internal/calendar/class-projections", Map.of(), null);
    }

    public Response calendarCancelClass(String id) {
        return apiClient.post("/calender-service/api/v1/internal/calendar/class-projections/" + id + "/cancel", Map.of(), null);
    }

    public Response calendarUpsertAttendance() {
        return apiClient.post("/calender-service/api/v1/internal/calendar/attendance-projections", Map.of(), null);
    }

    public Response calendarCancelAttendance(String id) {
        return apiClient.post("/calender-service/api/v1/internal/calendar/attendance-projections/" + id + "/cancel", Map.of(), null);
    }

    public Response calendarUpsertExam() {
        return apiClient.post("/calender-service/api/v1/internal/calendar/exam-projections", Map.of(), null);
    }

    public Response calendarCancelExam(String id) {
        return apiClient.post("/calender-service/api/v1/internal/calendar/exam-projections/" + id + "/cancel", Map.of(), null);
    }

    public Response courseInternalLesson(String id) {
        return apiClient.get("/course-service/api/v1/internal/lessons/" + id, null, Map.of(), Map.of());
    }

    public Response courseMaterialScope(String id) {
        return apiClient.get("/course-service/api/v1/internal/material-scopes/courses/" + id, null, Map.of(), Map.of());
    }

    public Response moduleMaterialScope(String id) {
        return apiClient.get("/course-service/api/v1/internal/material-scopes/modules/" + id, null, Map.of(), Map.of());
    }

    public Response lessonMaterialScope(String id) {
        return apiClient.get("/course-service/api/v1/internal/material-scopes/lessons/" + id, null, Map.of(), Map.of());
    }

    public Response examReconcile(String id) {
        return apiClient.post("/exam-service/api/v1/internal/exams/" + id + "/calendar-reconcile", Map.of(), null);
    }

    public Response materialAssociate(String id) {
        return apiClient.post("/material-service/api/v1/internal/materials/" + id + "/association", Map.of(), null);
    }

    public Response materialGetAssociation(String id) {
        return apiClient.get("/material-service/api/v1/internal/materials/" + id + "/association", null, Map.of(), Map.of());
    }

    public Response userAuthProfile() {
        return apiClient.get("/user-service/api/v1/internal/users/auth-profile", null, Map.of(), Map.of());
    }

    public Response userActivate() {
        return apiClient.post("/user-service/api/v1/internal/users/auth-profile/activate", Map.of(), null);
    }

    public Response userRecordLastLogin() {
        return apiClient.post("/user-service/api/v1/internal/users/auth-profile/last-login", Map.of(), null);
    }
}
