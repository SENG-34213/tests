package com.mentify.tests.course;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class CourseClient {

    private static final String COURSE_PATH = "/api/v1/course";

    private final ApiClient apiClient;

    public CourseClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response myCourses(String bearerToken) {
        return apiClient.get(COURSE_PATH + "/my-courses", bearerToken, Map.of(), Map.of());
    }

    public Response getAllCourses(String bearerToken) {
        return apiClient.get(COURSE_PATH, bearerToken, Map.of(), Map.of());
    }

    public Response hello() {
        return apiClient.get("/");
    }

    public Response lookup(String courseId, String bearerToken) {
        return apiClient.get(coursePath(courseId) + "/lookup", bearerToken, Map.of(), Map.of());
    }

    public Response getById(String courseId, String bearerToken) {
        return apiClient.get(coursePath(courseId), bearerToken, Map.of(), Map.of());
    }

    public Response create(Object request, String bearerToken) {
        return apiClient.post(COURSE_PATH, request, bearerToken);
    }

    public Response update(String courseId, Object request, String bearerToken) {
        return apiClient.put(coursePath(courseId), request, bearerToken);
    }

    public Response bulkLookup(Object request, String bearerToken) {
        return apiClient.post(COURSE_PATH + "/bulk", request, bearerToken);
    }

    public Response teacherDashboard(String bearerToken) {
        return apiClient.get("/api/v1/teacher/dashboard", bearerToken, Map.of(), Map.of());
    }

    public Response teacherSearch(String query, String bearerToken) {
        return teacherSearch(query, null, null, bearerToken);
    }

    public Response teacherSearch(String query, String types, Integer limit, String bearerToken) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        Map<String, Object> queryParameters = new java.util.LinkedHashMap<>();
        queryParameters.put("q", query);
        if (types != null) {
            queryParameters.put("types", types);
        }
        if (limit != null) {
            queryParameters.put("limit", limit);
        }
        return apiClient.get("/api/v1/teacher/search", bearerToken, queryParameters, Map.of());
    }

    public Response teacherStudents(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/students", bearerToken,
                Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherStudentSummary(String studentId, String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/students/" + requiredId(studentId, "studentId") + "/summary",
                bearerToken, Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherGradebook(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/teacher/gradebook", bearerToken,
                Map.of("courseId", requiredId(courseId, "courseId")), Map.of());
    }

    public Response teacherAnalytics(String courseId, String bearerToken) {
        return teacherAnalytics(courseId, null, null, null, bearerToken);
    }

    public Response teacherAnalytics(String courseId, String period, String from, String to, String bearerToken) {
        Map<String, Object> query = new java.util.LinkedHashMap<>();
        query.put("courseId", requiredId(courseId, "courseId"));
        if (period != null) {
            query.put("period", period);
        }
        if (from != null) {
            query.put("from", from);
        }
        if (to != null) {
            query.put("to", to);
        }
        return apiClient.get("/api/v1/teacher/analytics", bearerToken, query, Map.of());
    }

    public Response createModule(String courseId, Object request, String bearerToken) {
        return apiClient.post(modulesPath(courseId), request, bearerToken);
    }

    public Response modules(String courseId, String bearerToken) {
        return apiClient.get(modulesPath(courseId), bearerToken, Map.of(), Map.of());
    }

    public Response module(String courseId, String moduleId, String bearerToken) {
        return apiClient.get(modulePath(courseId, moduleId), bearerToken, Map.of(), Map.of());
    }

    public Response updateModule(String courseId, String moduleId, Object request, String bearerToken) {
        return apiClient.put(modulePath(courseId, moduleId), request, bearerToken);
    }

    public Response deleteModule(String courseId, String moduleId, String bearerToken) {
        return apiClient.delete(modulePath(courseId, moduleId), bearerToken);
    }

    public Response createLesson(String courseId, String moduleId, Object request, String bearerToken) {
        return apiClient.post(modulePath(courseId, moduleId) + "/lessons", request, bearerToken);
    }

    public Response lessons(String courseId, String moduleId, String bearerToken) {
        return apiClient.get(modulePath(courseId, moduleId) + "/lessons", bearerToken, Map.of(), Map.of());
    }

    public Response lesson(String courseId, String moduleId, String lessonId, String bearerToken) {
        return apiClient.get(lessonPath(courseId, moduleId, lessonId), bearerToken, Map.of(), Map.of());
    }

    public Response updateLesson(String courseId, String moduleId, String lessonId,
                                 Object request, String bearerToken) {
        return apiClient.put(lessonPath(courseId, moduleId, lessonId), request, bearerToken);
    }

    public Response deleteLesson(String courseId, String moduleId, String lessonId, String bearerToken) {
        return apiClient.delete(lessonPath(courseId, moduleId, lessonId), bearerToken);
    }

    public Response createLearningMaterial(String courseId, String moduleId, Object request, String bearerToken) {
        return apiClient.post(modulePath(courseId, moduleId) + "/learning-materials", request, bearerToken);
    }

    public Response uploadModuleMaterial(String courseId, String moduleId, String title,
                                         String description, File file, String bearerToken) {
        return apiClient.postMultipart(modulePath(courseId, moduleId) + "/learning-materials/upload",
                Map.of("title", requiredId(title, "title"), "description", description), file, bearerToken);
    }

    public Response uploadLessonMaterial(String courseId, String moduleId, String lessonId, String title,
                                         String description, File file, String bearerToken) {
        return apiClient.postMultipart(lessonPath(courseId, moduleId, lessonId) + "/materials/upload",
                Map.of("title", requiredId(title, "title"), "description", description), file, bearerToken);
    }

    public Response learningMaterials(String courseId, String moduleId, String bearerToken) {
        return apiClient.get(modulePath(courseId, moduleId) + "/learning-materials",
                bearerToken, Map.of(), Map.of());
    }

    public Response lessonMaterials(String courseId, String moduleId, String lessonId, String bearerToken) {
        return apiClient.get(lessonPath(courseId, moduleId, lessonId) + "/materials",
                bearerToken, Map.of(), Map.of());
    }

    public Response learningMaterial(String courseId, String moduleId, String materialId, String bearerToken) {
        return apiClient.get(modulePath(courseId, moduleId) + "/learning-materials/"
                + requiredId(materialId, "materialId"), bearerToken, Map.of(), Map.of());
    }

    public Response updateLearningMaterial(String courseId, String moduleId, String materialId,
                                           Object request, String bearerToken) {
        return apiClient.put(modulePath(courseId, moduleId) + "/learning-materials/"
                + requiredId(materialId, "materialId"), request, bearerToken);
    }

    public Response deleteLearningMaterial(String courseId, String moduleId, String materialId,
                                           String bearerToken) {
        return apiClient.delete(modulePath(courseId, moduleId) + "/learning-materials/"
                + requiredId(materialId, "materialId"), bearerToken);
    }

    public Response grades(String bearerToken) {
        return apiClient.get("/api/v1/course/grades", bearerToken, Map.of(), Map.of());
    }

    public Response grade(String gradeId, String bearerToken) {
        return apiClient.get(gradePath(gradeId), bearerToken, Map.of(), Map.of());
    }

    public Response createGrade(Object request, String bearerToken) {
        return apiClient.post("/api/v1/course/grades", request, bearerToken);
    }

    public Response updateGrade(String gradeId, Object request, String bearerToken) {
        return apiClient.put(gradePath(gradeId), request, bearerToken);
    }

    public Response deleteGrade(String gradeId, String bearerToken) {
        return apiClient.delete(gradePath(gradeId), bearerToken);
    }

    public Response monthlyFees(String bearerToken) {
        return apiClient.get("/api/v1/course/grades/monthly-fees", bearerToken, Map.of(), Map.of());
    }

    public Response monthlyFee(String gradeId, String bearerToken) {
        return apiClient.get(gradePath(gradeId) + "/monthly-fee", bearerToken, Map.of(), Map.of());
    }

    public Response createMonthlyFee(String gradeId, Object request, String bearerToken) {
        return apiClient.post(gradePath(gradeId) + "/monthly-fee", request, bearerToken);
    }

    public Response updateMonthlyFee(String gradeId, Object request, String bearerToken) {
        return apiClient.put(gradePath(gradeId) + "/monthly-fee", request, bearerToken);
    }

    public Response deleteMonthlyFee(String gradeId, String bearerToken) {
        return apiClient.delete(gradePath(gradeId) + "/monthly-fee", bearerToken);
    }

    public Response myAnnouncements(String courseId, int page, int size, String bearerToken) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
        Map<String, Object> query = new java.util.LinkedHashMap<>();
        if (courseId != null) {
            query.put("courseId", requiredId(courseId, "courseId"));
        }
        query.put("page", page);
        query.put("size", size);
        return apiClient.get("/api/v1/announcements/me", bearerToken, query, Map.of());
    }

    public Response createAnnouncement(String courseId, Object request, String bearerToken) {
        return apiClient.post("/api/v1/announcements/courses/" + requiredId(courseId, "courseId"),
                request, bearerToken);
    }

    public Response myProgress(String bearerToken) {
        return apiClient.get("/api/v1/progress/me", bearerToken, Map.of(), Map.of());
    }

    public Response courseProgress(String courseId, String bearerToken) {
        return apiClient.get("/api/v1/progress/courses/" + requiredId(courseId, "courseId"),
                bearerToken, Map.of(), Map.of());
    }

    public Response updateLessonProgress(String lessonId, Object request, String bearerToken) {
        return apiClient.put("/api/v1/progress/lessons/" + requiredId(lessonId, "lessonId"),
                request, bearerToken);
    }

    public Response delete(String courseId, String bearerToken) {
        return apiClient.delete(coursePath(courseId), bearerToken);
    }

    private static String coursePath(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId must not be blank");
        }
        return COURSE_PATH + "/" + courseId;
    }

    private static String requiredId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String modulesPath(String courseId) {
        return "/api/v1/course/" + requiredId(courseId, "courseId") + "/modules";
    }

    private static String modulePath(String courseId, String moduleId) {
        return modulesPath(courseId) + "/" + requiredId(moduleId, "moduleId");
    }

    private static String lessonPath(String courseId, String moduleId, String lessonId) {
        return modulePath(courseId, moduleId) + "/lessons/" + requiredId(lessonId, "lessonId");
    }

    private static String gradePath(String gradeId) {
        return "/api/v1/course/grades/" + requiredId(gradeId, "gradeId");
    }
}
