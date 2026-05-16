package com.financewave.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financewave.auth.entity.RefreshToken;
import com.financewave.auth.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository repo;

    // ✅ Create refresh token
    public String createRefreshToken(String username) {

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        repo.save(token);

        return token.getToken();
    }

    // ✅ Validate refresh token
    public boolean validate(String token) {

        Optional<RefreshToken> optionalToken = repo.findByToken(token);

        if (optionalToken.isEmpty()) {
            return false;
        }

        RefreshToken rt = optionalToken.get();

        return rt.getExpiryDate().isAfter(LocalDateTime.now());
    }

    // ✅ Get username from refresh token
    public String getUsername(String token) {

        return repo.findByToken(token)
                .map(RefreshToken::getUsername)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }
}