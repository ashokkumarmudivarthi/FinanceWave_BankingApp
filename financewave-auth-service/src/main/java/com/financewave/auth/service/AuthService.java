package com.financewave.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.financewave.auth.dto.*;
import com.financewave.auth.entity.User;
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

    // ✅ REGISTER
    public ApiResponse<UserResponse> register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ApiResponse<UserResponse>("FAILURE", "Username already exists", null);
        	/*return new ApiResponse<UserResponse>(
        	        "FAILURE",
        	        "Username already exists",
        	        LocalDateTime.now(),
        	        null
        	);*/
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

        return new ApiResponse<UserResponse>("SUCCESS", "User registered successfully", res);
        /*return new ApiResponse<UserResponse>(
                "SUCCESS",
                "User registered successfully",
                LocalDateTime.now(),
                res
        );*/
    }

    // ✅ LOGIN
    public ApiResponse<AuthResponse> login(LoginRequest req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new ApiResponse<AuthResponse>("FAILURE", "Invalid credentials", null);
        	/*return new ApiResponse<AuthResponse>(
        	        "FAILURE",
        	        "Invalid credentials",
        	        LocalDateTime.now(),
        	        null
        	);*/
        	
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refresh = refreshService.createRefreshToken(user.getUsername());

        AuthResponse response = new AuthResponse(
                token,
                refresh,
                user.getUsername(),
                user.getRole()
        );

       // return new ApiResponse<AuthResponse>("SUCCESS", "Login successful", response);
        return new ApiResponse<AuthResponse>(
                "SUCCESS",
                "Login successful",
                LocalDateTime.now(),
                response
        );
    }

    // ✅ GENERATE ACCESS TOKEN (FOR REFRESH)
    public String generateAccessToken(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}