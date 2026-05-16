package com.financewave.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.financewave.auth.dto.*;
import com.financewave.auth.entity.LoginAudit;
import com.financewave.auth.entity.User;
import com.financewave.auth.repository.LoginAuditRepository;
import com.financewave.auth.repository.UserRepository;
import com.financewave.auth.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshService;

    @Autowired
    private LoginAuditRepository auditRepo;

    @Autowired
    private OtpService otpService;

    // =========================
    // ✅ REGISTER
    // =========================
    public ApiResponse<UserResponse> register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ApiResponse<>("FAILURE", "Username already exists", null);
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        user.setActive(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        UserResponse res = new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        return new ApiResponse<>("SUCCESS", "User registered successfully", res);
    }

    // =========================
    // ✅ LOGIN
    // =========================
    public AuthResponse login(LoginRequest req, String ipAddress) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔒 Check account lock
        if (!user.isAccountNonLocked()) {
            saveAudit(user.getUsername(), "ACCOUNT_LOCKED", ipAddress);
            throw new RuntimeException("Account is locked. Contact support.");
        }

        // ❌ Wrong password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setAccountNonLocked(false);
            }

            userRepository.save(user);
            saveAudit(user.getUsername(), "FAILURE", ipAddress);

            throw new RuntimeException("Invalid credentials");
        }

        // ✅ SUCCESS LOGIN
        user.setFailedAttempts(0);
        userRepository.save(user);

        saveAudit(user.getUsername(), "SUCCESS", ipAddress);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refresh = refreshService.createRefreshToken(user.getUsername());

        return new AuthResponse(token, refresh, user.getUsername(), user.getRole());
    }

    // =========================
    // ✅ GENERATE TOKEN
    // =========================
    public String generateAccessToken(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    // =========================
    // ✅ FORGOT PASSWORD (SEND OTP)
    // =========================
    public void forgotPassword(String email) {
        otpService.sendOtp(email);
    }

    // =========================
    // ✅ RESET PASSWORD
    // =========================
    public void resetPassword(String email, String otp, String newPassword) {

        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // =========================
    // ✅ AUDIT LOGGER
    // =========================
    private void saveAudit(String username, String status, String ip) {

        LoginAudit audit = new LoginAudit();
        audit.setUsername(username);
        audit.setStatus(status);
        audit.setIpAddress(ip);
        audit.setTimestamp(LocalDateTime.now());

        auditRepo.save(audit);
    }
}