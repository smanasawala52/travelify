package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends TravelifyException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }
}
