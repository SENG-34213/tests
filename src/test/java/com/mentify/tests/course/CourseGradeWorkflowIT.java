package com.mentify.tests.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mentify.tests.auth.AuthenticationSession;
import com.mentify.tests.auth.TestRole;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseGradeWorkflowIT extends CourseIntegrationTestSupport {

    private static final String WRITE_FLAG = "TEST_COURSE_GRADE_WRITE_ENABLED";

    @BeforeEach
    void requireExplicitDisposableGradeWrites() {
        Assumptions.assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault(WRITE_FLAG, "false")),
                "Set TEST_COURSE_GRADE_WRITE_ENABLED=true only for an isolated non-production backend");
    }

    @Test
    void shouldManageGradeAndMonthlyFeeThenDeleteCreatedRecords() {
        AuthenticationSession admin = login(TestRole.ADMIN);
        String gradeId = null;
        boolean feeCreated = false;
        try {
            Response created = courseClient.createGrade(gradeRequest("TEST grade"), admin.accessToken());
            assertEquals(201, created.statusCode(), created.asString());
            gradeId = created.jsonPath().getString("data.id");
            assertNotNull(gradeId, created.asString());

            assertEquals(200, courseClient.grades(admin.accessToken()).statusCode());
            assertEquals(200, courseClient.grade(gradeId, admin.accessToken()).statusCode());
            assertEquals(200, courseClient.updateGrade(gradeId,
                    gradeRequest("TEST updated grade"), admin.accessToken()).statusCode());
            assertEquals("TEST updated grade",
                    courseClient.grade(gradeId, admin.accessToken()).jsonPath().getString("data.name"));

            Response fee = courseClient.createMonthlyFee(gradeId, feeRequest("35.50"), admin.accessToken());
            assertEquals(201, fee.statusCode(), fee.asString());
            feeCreated = true;
            assertEquals(200, courseClient.monthlyFee(gradeId, admin.accessToken()).statusCode());
            assertEquals(200, courseClient.monthlyFees(admin.accessToken()).statusCode());

            Response updatedFee = courseClient.updateMonthlyFee(gradeId, feeRequest("40.00"), admin.accessToken());
            assertEquals(200, updatedFee.statusCode(), updatedFee.asString());
            assertEquals(0, new BigDecimal(updatedFee.jsonPath().getString("data.monthlyFee"))
                    .compareTo(new BigDecimal("40.00")));

            assertEquals(200, courseClient.deleteMonthlyFee(gradeId, admin.accessToken()).statusCode());
            feeCreated = false;
            assertEquals(200, courseClient.deleteGrade(gradeId, admin.accessToken()).statusCode());
            gradeId = null;
        } finally {
            if (feeCreated && gradeId != null) {
                courseClient.deleteMonthlyFee(gradeId, admin.accessToken());
            }
            if (gradeId != null) {
                courseClient.deleteGrade(gradeId, admin.accessToken());
            }
            logout(admin);
        }
    }

    private static Map<String, Object> gradeRequest(String name) {
        return Map.of("name", name + " " + System.nanoTime(),
                "description", "Disposable grade created by integration test",
                "monthlyFee", new BigDecimal("25.00"),
                "currencyCode", "LKR");
    }

    private static Map<String, Object> feeRequest(String amount) {
        return Map.of("monthlyFee", new BigDecimal(amount), "currencyCode", "LKR");
    }
}
