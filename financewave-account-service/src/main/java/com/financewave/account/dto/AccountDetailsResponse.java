package com.financewave.account.dto;

import java.time.LocalDateTime;

public class AccountDetailsResponse {

    private String accountNumber;
    private String accountType;
    private double balance;
    private String status;
    private LocalDateTime createdAt;

    public AccountDetailsResponse(String accountNumber, String accountType,
                                  double balance, String status,
                                  LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    // getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}