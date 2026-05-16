package com.financewave.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;
    private String role;

    private String email;
    private String phoneNumber;

    private String firstName;
    private String lastName;

    private boolean active = true;
    private boolean accountNonLocked = true;

    private int failedAttempts = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}