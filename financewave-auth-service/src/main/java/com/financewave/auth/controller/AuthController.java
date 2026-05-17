package com.financewave.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.financewave.auth.dto.*;
import com.financewave.auth.service.*;
import com.financewave.auth.security.JwtUtil;

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

    @Autowired
    private RateLimiterService rateLimiter;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // ✅ REGISTER
    // =========================
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    // =========================
    // ✅ LOGIN
    // =========================
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        rateLimiter.validate(ip);

        AuthResponse response = authService.login(req, ip);

        return new ApiResponse<>("SUCCESS", "Login successful", response);
    }

    // =========================
    // ✅ REFRESH TOKEN
    // =========================
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

    // =========================
    // ✅ FORGOT PASSWORD (SEND OTP)
    // =========================
    @PostMapping("/forgot-password")
    public ApiResponse<String> sendOtp(
            @RequestParam String email,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        rateLimiter.validate(ip);

        otpService.sendOtp(email);

        return new ApiResponse<>("SUCCESS", "OTP sent successfully", null);
    }

    // =========================
    // ✅ RESEND OTP
    // =========================
    @PostMapping("/resend-otp")
    public ApiResponse<String> resendOtp(
            @RequestParam String email,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        rateLimiter.validate(ip);

        otpService.sendOtp(email);

        return new ApiResponse<>("SUCCESS", "OTP resent successfully", null);
    }

    // =========================
    // ✅ VERIFY OTP
    // =========================
    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpRequest req) {

        authService.verifyOtp(req.getEmail(), req.getOtp());

        return new ApiResponse<>("SUCCESS", "OTP verified successfully", null);
    }

    // =========================
    // ✅ RESET PASSWORD
    // =========================
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return new ApiResponse<>("SUCCESS", "Password reset successful", null);
    }

    // =========================
    // ✅ CHANGE PASSWORD (LOGGED IN USER)
    // =========================
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(
            @RequestBody ChangePasswordRequest req,
            @RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);

        authService.changePassword(req);

        return new ApiResponse<>("SUCCESS", "Password changed successfully", null);
    }

    // =========================
    // ✅ ADMIN UNLOCK USER
    // =========================
    @PostMapping("/admin/unlock")
    public ApiResponse<String> unlockUser(@RequestParam String username) {
        return authService.unlockUser(username);
    }

    // =========================
    // ✅ LOGOUT (SINGLE SESSION)
    // =========================
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        blacklistService.blacklist(token);
        sessionService.invalidateSession(token);

        return new ApiResponse<>("SUCCESS", "Logged out successfully", null);
    }

    // =========================
    // ✅ LOGOUT ALL (SELF)
    // =========================
    @PostMapping("/logout-all")
    public ApiResponse<String> logoutAll(HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String username = jwtUtil.extractUsername(token);

        sessionService.invalidateAllSessions(username);

        return new ApiResponse<>("SUCCESS", "All sessions logged out", null);
    }

    // =========================
    // ✅ ADMIN FORCE LOGOUT
    // =========================
    @PostMapping("/admin/logout-all")
    public ApiResponse<String> adminLogoutAll(@RequestParam String username) {

        sessionService.invalidateAllSessions(username);

        return new ApiResponse<>("SUCCESS", "User sessions terminated by admin", null);
    }

    // =========================
    // ✅ VIEW OWN SESSIONS
    // =========================
    @GetMapping("/sessions")
    public ApiResponse<Object> mySessions(HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String username = jwtUtil.extractUsername(token);

        Object data = authService.getSessions(username);

        return new ApiResponse<>("SUCCESS", "Active sessions fetched", data);
    }
}