package com.financewave.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.financewave.auth.dto.*;
import com.financewave.auth.service.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshService;

    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private OtpService otpService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    // ✅ LOGIN (ONLY ONE METHOD — FIXED)
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        AuthResponse response = authService.login(req, ip);

        return new ApiResponse<>("SUCCESS", "Login successful", response);
    }

    // ✅ REFRESH
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestParam String refreshToken) {

        if (!refreshService.validate(refreshToken)) {
            return new ApiResponse<>("FAILURE", "Invalid refresh token", null);
        }

        String username = refreshService.getUsername(refreshToken);
        String newAccessToken = authService.generateAccessToken(username);

        AuthResponse response = new AuthResponse(
                newAccessToken,
                refreshToken,
                username,
                "REFRESHED"
        );

        return new ApiResponse<>("SUCCESS", "Token refreshed successfully", response);
    }
    
    
 // ✅ SEND OTP
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam String email) {

        otpService.sendOtp(email);

        return new ApiResponse<>("SUCCESS", "OTP sent to email", null);
    }

  /*  // ✅ RESET PASSWORD
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        authService.resetPassword(email, otp, newPassword);

        return new ApiResponse<>("SUCCESS", "Password reset successful", null);
    }*/
    
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return new ApiResponse<>("SUCCESS", "Password reset successful", null);
    }
    
    @PostMapping("/admin/unlock")
    public ApiResponse<String> unlockUser(@RequestParam String username) {
        return authService.unlockUser(username);
    }

    // ✅ LOGOUT
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        blacklistService.blacklist(token);

        return new ApiResponse<>("SUCCESS", "Logged out successfully", null);
    }
}