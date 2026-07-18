package com.travelify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TravelifyException.class)
    public ResponseEntity<Map<String, Object>> handleTravelify(TravelifyException ex) {
        return body(ex.getStatus(), ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return body(ex.getStatus(), ex.getMessage(), "API_ERROR");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> payload = base(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR");
        payload.put("fields", fields);
        return ResponseEntity.badRequest().body(payload);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Authentication failed", "UNAUTHORIZED");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() == null ? "Unexpected error" : ex.getMessage(),
                "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status).body(base(status, message, code));
    }

    private Map<String, Object> base(HttpStatus status, String message, String code) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", message);
        payload.put("code", code);
        return payload;
    }
}
