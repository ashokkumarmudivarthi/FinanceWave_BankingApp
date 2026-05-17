package com.financewave.transaction.dto;

public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;

    // ✅ REQUIRED CONSTRUCTOR
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // ✅ GETTERS
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    // (Optional but good)
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
}