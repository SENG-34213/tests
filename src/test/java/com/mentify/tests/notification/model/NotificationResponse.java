package com.mentify.tests.notification.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String type, String title, String message, String sourceService,
                                   Instant occurredAt, Instant createdAt, Instant readAt, String emailStatus) {
}
