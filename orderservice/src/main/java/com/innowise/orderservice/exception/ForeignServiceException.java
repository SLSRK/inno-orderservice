package com.innowise.orderservice.exception;

public class ForeignServiceException extends RuntimeException {
    public ForeignServiceException(String message, Throwable throwable) {
        super(message);
    }
}
