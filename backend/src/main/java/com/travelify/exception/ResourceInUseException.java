package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class ResourceInUseException extends TravelifyException {
    public ResourceInUseException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_IN_USE");
    }
}
