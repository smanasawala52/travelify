package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends TravelifyException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
