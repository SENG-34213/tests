package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ContractConfigurationTest {

    @Test
    void contractValidationIsDisabledByDefault() {
        ContractConfiguration configuration = ContractConfiguration.fromEnvironment(Map.of());
        assertFalse(configuration.enabled());
        assertFalse(configuration.strict());
        assertFalse(configuration.failOnMissingFields());
        assertEquals("http://localhost:8080", configuration.baseUrl());
    }

    @Test
    void strictContractOptionsAreReadFromExternalConfiguration() {
        ContractConfiguration configuration = ContractConfiguration.fromEnvironment(Map.of(
                "CONTRACT_TEST_ENABLED", "true",
                "CONTRACT_SCHEMA_STRICT", "true",
                "CONTRACT_FAIL_ON_EXTRA_REQUIRED_FIELDS", "true",
                "CONTRACT_FAIL_ON_MISSING_FIELDS", "true",
                "CONTRACT_COURSE_ID", "course-fixture"));
        assertTrue(configuration.enabled());
        assertTrue(configuration.strict());
        assertTrue(configuration.failOnExtraRequiredFields());
        assertTrue(configuration.failOnMissingFields());
    }
}
