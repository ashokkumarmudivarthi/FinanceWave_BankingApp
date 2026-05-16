package com.financewave.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.financewave.auth.dto.*;
import com.financewave.auth.service.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshService;

    @Autowired
    private BlacklistService blacklistService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // ✅ REFRESH TOKEN
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestParam String refreshToken) {

        if (!refreshService.validate(refreshToken)) {
            return new ApiResponse<AuthResponse>("FAILURE", "Invalid refresh token", null);
        	/*return new ApiResponse<AuthResponse>(
        	        "FAILURE",
        	        "Invalid refresh token",
        	        java.time.LocalDateTime.now(),
        	        null
        	);*/
        }

        String username = refreshService.getUsername(refreshToken);
        String newAccessToken = authService.generateAccessToken(username);

        AuthResponse response = new AuthResponse(
                newAccessToken,
                refreshToken,
                username,
                "REFRESHED"
        );

        //return new ApiResponse<AuthResponse>("SUCCESS", "Token refreshed successfully", response);
        return new ApiResponse<AuthResponse>(
                "SUCCESS",
                "Token refreshed successfully",
                java.time.LocalDateTime.now(),
                response
        );
    }
   /* 
    @GetMapping("/user/profile")
    public ApiResponse<UserResponse> getProfile() {

        UserResponse user = new UserResponse("admin", "admin@test.com", "ADMIN");

        return new ApiResponse<>("SUCCESS", "Profile fetched", user);
    }
    
    @GetMapping("/admin/dashboard")
    public ApiResponse<String> dashboard() {
        return new ApiResponse<>("SUCCESS", "Dashboard loaded", "Admin Dashboard Data");
    }
    
    @GetMapping("/api/secure")
    public ApiResponse<String> secure() {
        return new ApiResponse<>("SUCCESS", "Secure API accessed", "Authorized");
    }*/

    // ✅ LOGOUT
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        blacklistService.blacklist(token);

        return new ApiResponse<>("SUCCESS", "Logged out successfully", null);
        /*return new ApiResponse<String>(
                "SUCCESS",
                "Logged out successfully",
                java.time.LocalDateTime.now(),
                null
        );*/
    }
}