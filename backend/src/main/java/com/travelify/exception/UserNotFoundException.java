package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends TravelifyException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
