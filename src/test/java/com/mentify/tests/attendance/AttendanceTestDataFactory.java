package com.mentify.tests.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class AttendanceTestDataFactory {

    private AttendanceTestDataFactory() {
    }

    static Map<String, Object> session(UUID courseId) {
        return Map.of("courseId", courseId.toString(), "title", "Integration attendance " + UUID.randomUUID(),
                "attendanceDate", LocalDate.now().toString(), "startTime", LocalTime.of(9, 0).toString(),
                "endTime", LocalTime.of(10, 0).toString());
    }

    static Map<String, Object> records(UUID studentId, String status) {
        return Map.of("records", List.of(Map.of("studentId", studentId.toString(), "status", status,
                "remarks", "TEST-015 integration record")));
    }
}
