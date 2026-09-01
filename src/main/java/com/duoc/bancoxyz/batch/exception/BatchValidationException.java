package com.duoc.bancoxyz.batch.exception;

public class BatchValidationException extends RuntimeException {

    public BatchValidationException(String message) {
        super(message);
    }

    public BatchValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
