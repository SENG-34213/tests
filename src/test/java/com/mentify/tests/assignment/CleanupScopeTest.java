package com.mentify.tests.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CleanupScopeTest {

    @Test
    void runsRemainingActionsAndRetainsMultipleCleanupFailures() {
        List<String> completed = new ArrayList<>();
        CleanupScope scope = new CleanupScope()
                .add(() -> {
                    completed.add("first");
                    throw new IOException("first cleanup failed");
                })
                .add(() -> {
                    completed.add("second");
                    throw new IOException("second cleanup failed");
                })
                .add(() -> completed.add("last"));

        CleanupScope.CleanupFailureException failure =
                assertThrows(CleanupScope.CleanupFailureException.class, scope::close);

        assertEquals(List.of("last", "second", "first"), completed);
        assertEquals(1, failure.getCause().getSuppressed().length);
    }

    @Test
    void tryWithResourcesKeepsThePrimaryFailureAndSuppressesCleanupFailure() {
        RuntimeException primaryFailure = new RuntimeException("test assertion failed");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (CleanupScope cleanup = new CleanupScope()
                    .add(() -> {
                        throw new IOException("cleanup failed");
                    })) {
                throw primaryFailure;
            }
        });

        assertSame(primaryFailure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
    }
}
