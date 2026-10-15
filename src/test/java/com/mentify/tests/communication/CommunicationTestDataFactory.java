package com.mentify.tests.communication;

import java.util.Map;
import java.util.UUID;

final class CommunicationTestDataFactory {

    private CommunicationTestDataFactory() {
    }

    static Map<String, Object> group(UUID courseId) {
        return Map.of("courseId", courseId.toString(), "name", "Integration group " + UUID.randomUUID(),
                "description", "TEST-016 integration fixture");
    }

    static Map<String, Object> student(UUID studentId) {
        return Map.of("studentId", studentId.toString());
    }

    static Map<String, Object> message(String content) {
        return Map.of("content", content);
    }

    static Map<String, Object> settings(boolean enabled) {
        return Map.of("readReceiptsEnabled", enabled, "lastSeenVisible", enabled,
                "profilePhotoVisible", enabled, "groupNotificationsEnabled", enabled,
                "directNotificationsEnabled", enabled);
    }
}
