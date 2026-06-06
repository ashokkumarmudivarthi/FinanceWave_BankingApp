package com.financewave.beneficiary.exception;

import com.financewave.beneficiary.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntime(RuntimeException ex) {

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(
                        "FAILURE",
                        ex.getMessage(),
                        null
                )
        );
    }
}