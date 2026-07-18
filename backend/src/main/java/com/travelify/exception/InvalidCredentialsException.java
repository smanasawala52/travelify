package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends TravelifyException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}
