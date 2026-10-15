package com.mentify.tests.environment;

public class BackendReadinessException extends RuntimeException {

    public BackendReadinessException(String message) {
        super(message);
    }
}
