package com.travelify.exception;

import org.springframework.http.HttpStatus;

public abstract class TravelifyException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    protected TravelifyException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
