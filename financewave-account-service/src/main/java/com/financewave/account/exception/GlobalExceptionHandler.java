package com.financewave.account.exception;

import com.financewave.account.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // BUSINESS EXCEPTIONS
    // =========================
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Object> handleRuntime(RuntimeException ex) {

        return new ApiResponse<>(
                "FAILURE",
                ex.getMessage(),
                null
        );
    }

    // =========================
    // GENERIC EXCEPTION
    // =========================
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleGeneric(Exception ex) {

        return new ApiResponse<>(
                "ERROR",
                "Something went wrong. Please try again.",
                null
        );
    }
}