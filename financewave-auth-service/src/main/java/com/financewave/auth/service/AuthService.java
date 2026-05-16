package com.financewave.auth.service;

import com.financewave.auth.dto.*;
import com.financewave.auth.entity.User;
import com.financewave.auth.repository.UserRepository;
import com.financewave.auth.security.JwtUtil;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    public ApiResponse<UserResponse> register(RegisterRequest request) {

        // Check if user exists
        repo.findByUsername(request.getUsername())
            .ifPresent(u -> {
                throw new RuntimeException("User already exists with username: " + request.getUsername());
            });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEmail(request.getEmail());

        User savedUser = repo.save(user);

        UserResponse response = UserResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();

        return ApiResponse.<UserResponse>builder()
                .status("SUCCESS")
                .message("Customer onboarding completed successfully")
                .timestamp(LocalDateTime.now())
                .data(response)
                .build();
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {

        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        AuthResponse authResponse = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole()
        );

        return ApiResponse.<AuthResponse>builder()
                .status("SUCCESS")
                .message("Authentication successful")
                .timestamp(LocalDateTime.now())
                .data(authResponse)
                .build();
    }

     
}