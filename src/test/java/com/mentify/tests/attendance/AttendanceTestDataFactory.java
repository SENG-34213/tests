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
                "attendanceDate", LocalDate.now().plusDays(1 + Math.floorMod(UUID.randomUUID().hashCode(), 365)).toString(),
                "startTime", LocalTime.of(9, 0).toString(),
                "endTime", LocalTime.of(10, 0).toString());
    }

    static Map<String, Object> records(UUID studentId, String status) {
        return Map.of("records", List.of(Map.of("studentId", studentId.toString(), "status", status,
                "remarks", "TEST-015 integration record")));
    }

    static Map<String, Object> records(List<String> studentIds, String status) {
        List<Map<String, String>> records = studentIds.stream()
                .map(studentId -> Map.of("studentId", studentId, "status", status,
                        "remarks", "TEST-015 integration record"))
                .toList();
        return Map.of("records", records);
    }

    static Map<String, Object> duplicateRecords(UUID studentId) {
        Map<String, Object> record = Map.of("studentId", studentId.toString(), "status", "PRESENT");
        return Map.of("records", List.of(record, record));
    }
}
