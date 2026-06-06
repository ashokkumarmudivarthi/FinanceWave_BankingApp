package com.financewave.loanservice.exception;

import com.financewave.loanservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNotFound(ResourceNotFoundException ex) {
        return new ApiResponse<>("FAILURE", ex.getMessage(), null);
    }

    // 🟡 BAD REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleBadRequest(Exception ex) {
        return new ApiResponse<>("FAILURE", ex.getMessage(), null);
    }

    // 🔴 GENERIC ERROR
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleGeneric(Exception ex) {
        return new ApiResponse<>("FAILURE", "Something went wrong", null);
    }
}