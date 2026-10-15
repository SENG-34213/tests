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
        return Map.of("email", "not-an-email", "firstName", "", "lastName", "", "role", "INVALID");
    }

}
