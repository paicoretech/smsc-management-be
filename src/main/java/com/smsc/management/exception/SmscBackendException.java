package com.smsc.management.exception;

public class SmscBackendException extends RuntimeException {

    public SmscBackendException(String message) {
        super(message);
    }

    public SmscBackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
