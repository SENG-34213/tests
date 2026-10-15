package com.mentify.tests.payment;

import com.mentify.tests.api.ApiClient;
import io.restassured.response.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PaymentClient {

    private static final String PATH = "/api/v1/payments";
    private final ApiClient apiClient;

    public PaymentClient(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient must not be null");
    }

    public Response startCoursePayment(Object request, String token) {
        return apiClient.post(PATH + "/course", request, token);
    }

    public Response myPayments(String token) {
        return apiClient.get(PATH + "/me", token, Map.of(), Map.of());
    }

    public Response myPayment(String paymentId, String token) {
        return apiClient.get(PATH + "/" + id(paymentId, "paymentId"), token, Map.of(), Map.of());
    }

    public Response paymentStatus(String paymentId, String token) {
        return apiClient.get(PATH + "/" + id(paymentId, "paymentId") + "/status", token, Map.of(), Map.of());
    }

    public Response adminPayments(String status, String studentId, String courseId, String createdFrom,
                                  String createdTo, String reference, int page, int size, String token) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("page must be non-negative and size must be positive");
        }
        Map<String, Object> query = new LinkedHashMap<>();
        put(query, "status", status);
        put(query, "studentId", studentId);
        put(query, "courseId", courseId);
        put(query, "createdFrom", createdFrom);
        put(query, "createdTo", createdTo);
        put(query, "reference", reference);
        query.put("page", page);
        query.put("size", size);
        return apiClient.get(PATH + "/admin", token, query, Map.of());
    }

    public Response adminPayment(String paymentId, String token) {
        return apiClient.get(PATH + "/admin/" + id(paymentId, "paymentId"), token, Map.of(), Map.of());
    }

    public Response verifySuccessfulPayment(String studentId, String courseId, String token) {
        return apiClient.get(PATH + "/internal/students/" + id(studentId, "studentId") + "/courses/"
                + id(courseId, "courseId") + "/successful", token, Map.of(), Map.of());
    }

    public Response refund(String paymentId, Object request, String token) {
        return apiClient.post(PATH + "/admin/" + id(paymentId, "paymentId") + "/refund", request, token);
    }

    public Response webhook(String payload, String signature) {
        Map<String, String> headers = signature == null ? Map.of() : Map.of("Stripe-Signature", signature);
        return apiClient.postWithHeaders(PATH + "/stripe/webhook", payload, null, headers);
    }

    private static void put(Map<String, Object> query, String name, String value) {
        if (value != null && !value.isBlank()) {
            query.put(name, value);
        }
    }

    private static String id(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
