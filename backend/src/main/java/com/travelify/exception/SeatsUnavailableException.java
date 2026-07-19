package com.travelify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatsUnavailableException extends RuntimeException {
    public SeatsUnavailableException(String message) {
        super(message);
    }
}
