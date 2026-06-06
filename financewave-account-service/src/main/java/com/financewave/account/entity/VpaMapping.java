package com.financewave.account.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vpa_mapping")
public class VpaMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vpa;

    private String accountNumber;

    private String username;

    // Getters & Setters
    public Long getId() { return id; }

    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
    }
}