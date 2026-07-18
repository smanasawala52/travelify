package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends TravelifyException {
    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_CONFLICT");
    }
}
