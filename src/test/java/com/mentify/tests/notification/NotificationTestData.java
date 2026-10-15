package com.mentify.tests.notification;

import java.util.UUID;

record NotificationTestData(UUID notificationId, boolean writeEnabled) {

    static NotificationTestData fromEnvironment() {
        return new NotificationTestData(uuid("TEST_NOTIFICATION_ID"), Boolean.parseBoolean(
                System.getenv().getOrDefault("TEST_NOTIFICATION_WRITE_ENABLED", "false")));
    }

    private static UUID uuid(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid UUID for " + name, exception);
        }
    }
}
