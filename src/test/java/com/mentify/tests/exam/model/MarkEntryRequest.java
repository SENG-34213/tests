package com.mentify.tests.exam.model;

import java.math.BigDecimal;
import java.util.UUID;

public record MarkEntryRequest(UUID studentId, String attendanceStatus, BigDecimal marksObtained, String remarks) {
}
