package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends TravelifyException {
    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
}
