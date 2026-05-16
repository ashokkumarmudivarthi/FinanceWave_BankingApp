package com.financewave.auth.service;

import com.financewave.auth.entity.UserSession;
import com.financewave.auth.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionService {

    @Autowired
    private UserSessionRepository repo;

    // ✅ CREATE SESSION
    public void createSession(String username, String token, String ip) {

        UserSession session = new UserSession();
        session.setUsername(username);
        session.setToken(token);
        session.setIpAddress(ip);
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(2));

        repo.save(session);
    }

    // ✅ VALIDATE SESSION
    public boolean isSessionValid(String token) {

        return repo.findByToken(token)
                .map(s -> s.isActive() && s.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    // ✅ LOGOUT CURRENT SESSION
    public void invalidateSession(String token) {

        repo.findByToken(token).ifPresent(s -> {
            s.setActive(false);
            repo.save(s);
        });
    }

    // ✅ LOGOUT ALL SESSIONS
    public void invalidateAllSessions(String username) {

        repo.findByUsernameAndActiveTrue(username)
                .forEach(s -> {
                    s.setActive(false);
                    repo.save(s);
                });
    }
    
    public Object getActiveSessions(String username) {
        return repo.findByUsernameAndActiveTrue(username);
    }
}