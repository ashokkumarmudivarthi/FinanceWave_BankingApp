package com.financewave.transaction.exception;

import com.financewave.transaction.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // BUSINESS ERRORS
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Object> handleRuntime(RuntimeException ex) {

        return new ApiResponse<>(
                "FAILURE",
                ex.getMessage(),
                null
        );
    }

    // FALLBACK (optional)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleGeneric(Exception ex) {

        return new ApiResponse<>(
                "ERROR",
                "Something went wrong",
                null
        );
    }
}