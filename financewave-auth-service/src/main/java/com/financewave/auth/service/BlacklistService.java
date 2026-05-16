package com.financewave.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financewave.auth.entity.BlacklistedToken;
import com.financewave.auth.repository.BlacklistedTokenRepository;

import java.time.LocalDateTime;

@Service
public class BlacklistService {

    @Autowired
    private BlacklistedTokenRepository repo;

    public void blacklist(String token) {
        BlacklistedToken bt = new BlacklistedToken();
        bt.setToken(token);
        bt.setExpiry(LocalDateTime.now().plusMinutes(30));
        repo.save(bt);
    }

    public boolean isBlacklisted(String token) {
        return repo.existsByToken(token);
    }
}