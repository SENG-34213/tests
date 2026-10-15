package com.mentify.tests.exam;

import com.mentify.tests.exam.model.BatchMarksEntryRequest;
import com.mentify.tests.exam.model.CreateExamRequest;
import com.mentify.tests.exam.model.MarkEntryRequest;
import com.mentify.tests.exam.model.UpdateExamRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

record ExamTestData(UUID courseId, UUID studentId, UUID secondStudentId, boolean writeEnabled) {

    static ExamTestData fromEnvironment() {
        return new ExamTestData(uuid("TEST_EXAM_COURSE_ID"), uuid("TEST_EXAM_STUDENT_ID"),
                uuid("TEST_EXAM_SECOND_STUDENT_ID"), Boolean.parseBoolean(
                        System.getenv().getOrDefault("TEST_EXAM_WRITE_ENABLED", "false")));
    }

    boolean fixturesConfigured() {
        return courseId != null && studentId != null;
    }

    CreateExamRequest createRequest() {
        return new CreateExamRequest(courseId, "TEST-017 exam " + UUID.randomUUID(),
                "Exam Service integration fixture", LocalDate.now().plusDays(30),
                LocalTime.of(9, 0), LocalTime.of(11, 0), BigDecimal.valueOf(100),
                BigDecimal.valueOf(40), "OTHER");
    }

    UpdateExamRequest updateRequest() {
        return new UpdateExamRequest("Updated TEST-017 exam", "Updated integration fixture",
                LocalDate.now().plusDays(31), LocalTime.of(10, 0), LocalTime.of(12, 0),
                BigDecimal.valueOf(100), BigDecimal.valueOf(50), "OTHER");
    }

    BatchMarksEntryRequest marksRequest() {
        return new BatchMarksEntryRequest(List.of(
                new MarkEntryRequest(studentId, "PRESENT", BigDecimal.valueOf(75), "TEST-017 mark")));
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
