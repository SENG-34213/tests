package com.mentify.tests.assignment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Runs registered cleanup actions in reverse registration order and retains all failures.
 */
final class CleanupScope implements AutoCloseable {

    private final Deque<CleanupAction> actions = new ArrayDeque<>();

    CleanupScope add(CleanupAction action) {
        actions.addFirst(Objects.requireNonNull(action, "cleanup action must not be null"));
        return this;
    }

    @Override
    public void close() {
        Throwable firstFailure = null;
        while (!actions.isEmpty()) {
            try {
                actions.removeFirst().run();
            } catch (Throwable failure) {
                if (firstFailure == null) {
                    firstFailure = failure;
                } else {
                    firstFailure.addSuppressed(failure);
                }
            }
        }
        if (firstFailure != null) {
            throw new CleanupFailureException(firstFailure);
        }
    }

    @FunctionalInterface
    interface CleanupAction {
        void run() throws Exception;
    }

    static final class CleanupFailureException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private CleanupFailureException(Throwable cause) {
            super("One or more cleanup actions failed", cause);
        }
    }
}
