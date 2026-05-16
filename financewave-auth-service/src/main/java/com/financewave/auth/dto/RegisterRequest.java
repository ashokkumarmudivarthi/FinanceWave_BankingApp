package com.financewave.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role;
    private String email;
    private String phoneNumber;

    private String firstName;
    private String lastName;
}