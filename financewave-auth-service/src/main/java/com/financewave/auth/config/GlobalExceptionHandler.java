package com.financewave.auth.config;

import com.financewave.auth.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException ex) {

        return ApiResponse.builder()
                .status("FAILURE")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }
}