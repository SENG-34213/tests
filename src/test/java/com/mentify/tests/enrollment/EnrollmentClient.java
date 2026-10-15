package com.mentify.tests.enrollment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;

public class EnrollmentClient {

    private static final String ENROLLMENT_PATH = "/api/v1/enrollments";
    private final ApiClient apiClient;

    public EnrollmentClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response getActiveEnrollments(String bearerToken) {
        return apiClient.get(ENROLLMENT_PATH, bearerToken, Map.of(), Map.of());
    }

    public Response create(Object request, String bearerToken) {
        return apiClient.post(ENROLLMENT_PATH, request, bearerToken);
    }

    public Response selfEnroll(Object request, String bearerToken) {
        return apiClient.post(ENROLLMENT_PATH + "/me", request, bearerToken);
    }

    public Response update(String enrollmentId, Object request, String bearerToken) {
        return apiClient.put(resourcePath(enrollmentId), request, bearerToken);
    }

    public Response membership(String studentId, String courseId, String bearerToken) {
        return apiClient.get(resourcePath("students", studentId, "courses", courseId, "exists"),
                bearerToken, Map.of(), Map.of());
    }

    public Response studentCourses(String studentId, String bearerToken) {
        return apiClient.get(resourcePath("students", studentId, "courses"),
                bearerToken, Map.of(), Map.of());
    }

    public Response courseStudents(String courseId, String bearerToken) {
        return apiClient.get(resourcePath("courses", courseId, "students"),
                bearerToken, Map.of(), Map.of());
    }

    public Response unenrolledStudents(String bearerToken) {
        return apiClient.get(ENROLLMENT_PATH + "/students/unenrolled",
                bearerToken, Map.of(), Map.of());
    }

    private static String resourcePath(String... segments) {
        StringBuilder path = new StringBuilder(ENROLLMENT_PATH);
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                throw new IllegalArgumentException("Enrollment path segments must not be blank");
            }
            path.append('/').append(segment);
        }
        return path.toString();
    }
}
