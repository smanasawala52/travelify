package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends TravelifyException {
    public ForbiddenOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
