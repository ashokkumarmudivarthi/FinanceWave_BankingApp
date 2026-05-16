/*package com.financewave.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;
    private String message;
    private LocalDateTime timestamp;
    private T data;
}*/

package com.financewave.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;
    private String message;
    private LocalDateTime timestamp;
    private T data;

    // ✅ AUTO TIMESTAMP CONSTRUCTOR
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now(); // 🔥 AUTO
        this.data = data;
    }
}