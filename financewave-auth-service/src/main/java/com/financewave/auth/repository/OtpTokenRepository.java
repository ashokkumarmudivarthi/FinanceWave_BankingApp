package com.financewave.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.financewave.auth.entity.OtpToken;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByEmail(String email);
}