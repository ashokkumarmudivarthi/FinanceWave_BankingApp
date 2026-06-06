package com.financewave.beneficiary.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // =========================
    // GET SIGNING KEY
    // =========================
    private Key getKey() {

        // 🔥 DEBUG: Check if secret is loaded
        System.out.println("JWT SECRET VALUE: " + secret);

        if (secret == null || secret.isEmpty()) {
            throw new RuntimeException("JWT secret is missing or not configured");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================
    // EXTRACT USERNAME
    // =========================
    public String extractUsername(String token) {

        // 🔥 DEBUG: Check token coming or not
        System.out.println("TOKEN RECEIVED: " + token);
        String username = getClaims(token).getSubject();

        System.out.println("JWT USERNAME: " + username);

       // return getClaims(token).getSubject();
        return username;
        
    }

    // =========================
    // EXTRACT ROLE
    // =========================
    public String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    // =========================
    // VALIDATE TOKEN
    // =========================
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT VALIDATION FAILED: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // GET CLAIMS
    // =========================
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}