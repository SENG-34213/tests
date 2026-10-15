package com.mentify.tests.exam.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateExamRequest(String title, String description, LocalDate examDate,
                                LocalTime startTime, LocalTime endTime, BigDecimal totalMarks,
                                BigDecimal passMarks, String type) {
}
