package com.financewave.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String token;

    private String ipAddress;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}