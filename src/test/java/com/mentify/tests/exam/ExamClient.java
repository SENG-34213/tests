package com.mentify.tests.exam;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;
import java.io.File;

public class ExamClient {

    private static final String PATH = "/api/v1/exams";
    private final ApiClient apiClient;

    public ExamClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response create(Object request, String token) {
        return apiClient.post(PATH, request, token);
    }

    public Response get(String examId, String token) {
        return apiClient.get(PATH + "/" + id(examId, "examId"), token, Map.of(), Map.of());
    }

    public Response listByCourse(String courseId, String token) {
        return apiClient.get(PATH + "/courses/" + id(courseId, "courseId"), token, Map.of(), Map.of());
    }

    public Response update(String examId, Object request, String token) {
        return apiClient.put(PATH + "/" + id(examId, "examId"), request, token);
    }

    public Response cancel(String examId, String token) {
        return apiClient.post(PATH + "/" + id(examId, "examId") + "/cancel", null, token);
    }

    public Response schedule(String examId, String token) {
        return apiClient.post(PATH + "/" + id(examId, "examId") + "/schedule", null, token);
    }

    public Response startMarking(String examId, String token) {
        return apiClient.post(PATH + "/" + id(examId, "examId") + "/start-marking", null, token);
    }

    public Response complete(String examId, String token) {
        return apiClient.post(PATH + "/" + id(examId, "examId") + "/complete", null, token);
    }

    public Response results(String examId, String token) {
        return apiClient.get(PATH + "/" + id(examId, "examId") + "/results", token, Map.of(), Map.of());
    }

    public Response summary(String examId, String token) {
        return apiClient.get(PATH + "/" + id(examId, "examId") + "/summary", token, Map.of(), Map.of());
    }

    public Response enterMarks(String examId, Object request, String token) {
        return apiClient.put(PATH + "/" + id(examId, "examId") + "/results", request, token);
    }

    public Response myResults(String token) {
        return apiClient.get(PATH + "/me", token, Map.of(), Map.of());
    }

    public Response myUpcoming(String token) {
        return apiClient.get(PATH + "/me/upcoming", token, Map.of(), Map.of());
    }

    public Response studentExam(String examId, String token) {
        return apiClient.get(PATH + "/" + id(examId, "examId") + "/student", token, Map.of(), Map.of());
    }

    public Response attachments(String examId, String token) {
        return apiClient.get(attachmentPath(examId), token, Map.of(), Map.of());
    }

    public Response uploadAttachment(String examId, File file, boolean studentVisible, String token) {
        return apiClient.postMultipart(attachmentPath(examId), Map.of("studentVisible", studentVisible),
                Objects.requireNonNull(file, "file must not be null"), token);
    }

    public Response downloadAttachment(String examId, String attachmentId, String token) {
        return apiClient.get(attachmentPath(examId) + "/" + id(attachmentId, "attachmentId") + "/download",
                token, Map.of(), Map.of());
    }

    public Response deleteAttachment(String examId, String attachmentId, String token) {
        return apiClient.delete(attachmentPath(examId) + "/" + id(attachmentId, "attachmentId"), token);
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String attachmentPath(String examId) {
        return PATH + "/" + id(examId, "examId") + "/attachments";
    }
}
