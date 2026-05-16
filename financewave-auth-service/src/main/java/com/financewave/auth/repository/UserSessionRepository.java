package com.financewave.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.financewave.auth.entity.UserSession;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    List<UserSession> findByUsernameAndActiveTrue(String username);
}