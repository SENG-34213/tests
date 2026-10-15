package com.mentify.tests.exam.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateExamRequest(UUID courseId, String title, String description, LocalDate examDate,
                                LocalTime startTime, LocalTime endTime, BigDecimal totalMarks,
                                BigDecimal passMarks, String type) {
}
