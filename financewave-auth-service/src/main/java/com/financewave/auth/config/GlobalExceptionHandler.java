package com.financewave.auth.config;

import com.financewave.auth.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Object> handleRuntimeException(RuntimeException ex) {

    	return new ApiResponse<>("FAILURE", ex.getMessage(), null);
    	/*return new ApiResponse<Object>(
    	        "FAILURE",
    	        ex.getMessage(),
    	        java.time.LocalDateTime.now(),
    	        null
    	);*/
    }
}