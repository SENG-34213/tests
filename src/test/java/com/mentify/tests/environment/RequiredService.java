package com.mentify.tests.environment;

import java.util.Locale;

public enum RequiredService {
    API_GATEWAY,
    KEYCLOAK,
    CONFIG_SERVER,
    EUREKA,
    USER_SERVICE,
    COURSE_SERVICE,
    ENROLLMENT_SERVICE,
    ASSIGNMENT_SERVICE,
    COMMUNICATION_SERVICE,
    ATTENDANCE_SERVICE,
    AI_SERVICE,
    EXAM_SERVICE,
    PAYMENT_SERVICE,
    MATERIAL_SERVICE,
    CALENDAR_SERVICE,
    NOTIFICATION_SERVICE;

    public static RequiredService parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown required service: " + value, exception);
        }
    }
}
