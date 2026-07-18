package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends TravelifyException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, Long id) {
        this(resource + " not found: " + id);
    }
}
