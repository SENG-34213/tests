package com.mentify.tests.foundation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestRepositorySmokeTest {

    @Test
    void shouldRunFoundationTestGivenConfiguredJUnit() {
        // Arrange
        int expectedStatus = 200;

        // Act
        int actualStatus = 200;

        // Assert
        assertEquals(expectedStatus, actualStatus);
    }
}
