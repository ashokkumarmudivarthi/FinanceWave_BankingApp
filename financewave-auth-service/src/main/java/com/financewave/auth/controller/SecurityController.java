package com.financewave.auth.controller;

import org.springframework.web.bind.annotation.*;

import com.financewave.auth.dto.ApiResponse;
import com.financewave.auth.dto.UserResponse;

@RestController
@RequestMapping("/api")
public class SecurityController {

    // ✅ SECURE API
    @GetMapping("/secure")
    public ApiResponse<String> secure() {
        return new ApiResponse<>(
                "SUCCESS",
                "Secure API accessed",
                "Authorized"
        );
    }

    // ✅ ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public ApiResponse<String> admin() {
        return new ApiResponse<>(
                "SUCCESS",
                "Admin dashboard fetched",
                "Admin Dashboard Data"
        );
    }

    // ✅ USER PROFILE
    @GetMapping("/user/profile")
    public ApiResponse<UserResponse> user() {

        // 🔥 Later replace with DB data (JWT username)
        UserResponse user = new UserResponse(
                "admin1",
                "admin@test.com",
                "ADMIN"
        );

        return new ApiResponse<>(
                "SUCCESS",
                "User profile fetched",
                user
        );
    }

    // ✅ EMPLOYEE API
    @GetMapping("/employee/data")
    public ApiResponse<String> employee() {
        return new ApiResponse<>(
                "SUCCESS",
                "Employee data fetched",
                "Employee Internal Data"
        );
    }
}