package com.financewave.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.financewave.auth.dto.*;
import com.financewave.auth.entity.LoginAudit;
import com.financewave.auth.entity.PasswordHistory;
import com.financewave.auth.entity.User;
import com.financewave.auth.repository.LoginAuditRepository;
import com.financewave.auth.repository.PasswordHistoryRepository;
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
    
    @Autowired
    private PasswordHistoryRepository passwordHistoryRepo;
    
    @Autowired
    private SessionService sessionService;
    
    
    
   

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
        
        PasswordHistory ph = new PasswordHistory();
        ph.setUsername(user.getUsername());
        ph.setPassword(user.getPassword());
        ph.setChangedAt(LocalDateTime.now());

        passwordHistoryRepo.save(ph);

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
        
     // ✅ CREATE SESSION
        sessionService.createSession(user.getUsername(), token, ipAddress);

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

        // 🔒 PASSWORD HISTORY CHECK
        var historyList = passwordHistoryRepo
                .findTop3ByUsernameOrderByChangedAtDesc(user.getUsername());

        for (PasswordHistory old : historyList) {
            if (passwordEncoder.matches(newPassword, old.getPassword())) {
                throw new RuntimeException("You cannot reuse last 3 passwords");
            }
        }

        // ✅ SAVE NEW PASSWORD
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // ✅ SAVE HISTORY
        PasswordHistory ph = new PasswordHistory();
        ph.setUsername(user.getUsername());
        ph.setPassword(user.getPassword());
        ph.setChangedAt(LocalDateTime.now());

        passwordHistoryRepo.save(ph);
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
    
    
    public ApiResponse<String> unlockUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);

        userRepository.save(user);

        return new ApiResponse<>("SUCCESS", "User unlocked successfully", null);
    }
    
 // ✅ CHANGE PASSWORD
    public void changePassword(ChangePasswordRequest req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
    
 // ✅ VERIFY OTP (OPTIONAL)
    public void verifyOtp(String email, String otp) {

        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }
    
 // ✅ RESEND OTP
    public void resendOtp(String email) {
        otpService.sendOtp(email);
    }
    
 // ✅ GET ACTIVE SESSIONS
  

    public Object getSessions(String username) {
        return sessionService.getActiveSessions(username);
    }
    
 // ✅ GET AUDIT LOGS
  

    public Object getAuditLogs(String username) {
        return auditRepo.findByUsername(username);
    }
    
}