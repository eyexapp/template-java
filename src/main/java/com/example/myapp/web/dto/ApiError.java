package com.example.myapp.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String message,
        List<FieldError> fieldErrors,
        LocalDateTime timestamp
) {
    public ApiError(int status, String error, String message) {
        this(status, error, message, null, LocalDateTime.now());
    }

    public ApiError(int status, String error, String message, List<FieldError> fieldErrors) {
        this(status, error, message, fieldErrors, LocalDateTime.now());
    }

    public record FieldError(String field, String message) {}
}
