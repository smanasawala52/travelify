package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends TravelifyException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}
