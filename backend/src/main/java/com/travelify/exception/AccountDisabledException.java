package com.travelify.exception;

import org.springframework.http.HttpStatus;

public class AccountDisabledException extends TravelifyException {
    public AccountDisabledException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
    }
}
