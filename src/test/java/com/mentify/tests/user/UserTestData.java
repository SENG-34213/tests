package com.mentify.tests.user;

import java.util.Map;

record UserTestData(String userId, boolean writeEnabled, boolean superAdminSupported) {

    static UserTestData fromEnvironment() {
        Map<String, String> environment = System.getenv();
        return new UserTestData(environment.get("TEST_USER_ID"), Boolean.parseBoolean(
                environment.getOrDefault("TEST_USER_WRITE_ENABLED", "false")), Boolean.parseBoolean(
                environment.getOrDefault("TEST_SUPER_ADMIN_SUPPORTED", "false")));
    }

    static Map<String, Object> invalidRegistration() {
        return Map.of("email", "invalid-email", "firstName", "", "lastName", "", "role", "STUDENT");
    }

    static Map<String, Object> validRegistration() {
        return validRegistration("step4-denied-user@mentify.test");
    }

    static Map<String, Object> validRegistration(String email) {
        return Map.of(
                "email", email,
                "firstName", "Step Four",
                "lastName", "Denied User",
                "role", "STUDENT",
                "studentProfile", Map.of(
                        "dateOfBirth", "2000-01-01",
                        "guardianName", "Test Guardian",
                        "guardianPhone", "0771234567",
                        "attendanceMode", "ONLINE",
                        "grade", "08",
                        "schoolName", "Mentify Test School"),
                "address", Map.of("addressLine1", "1 Test Street", "city", "Colombo", "district", "Colombo"));
    }

    static Map<String, Object> validPublicStudentRegistration(String email) {
        return Map.of(
                "email", email,
                "firstName", "Synthetic",
                "lastName", "Public Student",
                "schoolName", "Mentify Test School",
                "dateOfBirth", "2000-01-01",
                "guardianName", "Synthetic Guardian",
                "guardianPhone", "0771234567",
                "attendanceMode", "ONLINE",
                "address", Map.of(
                        "addressLine1", "1 Synthetic Test Street",
                        "city", "Colombo",
                        "district", "Colombo"));
    }

    static Map<String, Object> validAdminRegistration() {
        return validAdminRegistration("step4-denied-admin@mentify.test");
    }

    static Map<String, Object> validAdminRegistration(String email) {
        return Map.of(
                "email", email,
                "firstName", "Step Four",
                "lastName", "Denied Admin",
                "adminProfile", Map.of(),
                "address", Map.of("addressLine1", "1 Test Street", "city", "Colombo", "district", "Colombo"));
    }

}
