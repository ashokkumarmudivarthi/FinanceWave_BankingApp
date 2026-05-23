package com.financewave.account.service;

import com.financewave.account.dto.*;
import com.financewave.account.entity.Account;
import com.financewave.account.repository.AccountRepository;
import com.financewave.account.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // CREATE ACCOUNT
    // =========================
    public ApiResponse<AccountResponse> create(String token, CreateAccountRequest req) {

        String username = jwtUtil.extractUsername(token);

        if (req.getInitialBalance() < 1000) {
            throw new RuntimeException("Minimum balance 1000 required");
        }

        Account acc = new Account();
        acc.setAccountNumber("FW" + System.currentTimeMillis());
        acc.setUsername(username);
        acc.setAccountType(req.getAccountType());
        acc.setBalance(req.getInitialBalance());
        acc.setStatus("ACTIVE");
        acc.setCreatedAt(LocalDateTime.now());

        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account created",
                new AccountResponse(
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getStatus()
                ));
    }

    // =========================
    // VALIDATE ACCOUNT
    // =========================
    public boolean validateAccount(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElse(null);

        return acc != null && "ACTIVE".equals(acc.getStatus());
    }

    // =========================
    // DEPOSIT
    // =========================
    @Transactional
    public ApiResponse<String> deposit(String accNo, double amount) {

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!"ACTIVE".equals(acc.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        acc.setBalance(acc.getBalance() + amount);
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Amount deposited successfully", null);
    }

    // =========================
    // WITHDRAW
    // =========================
    @Transactional
    public ApiResponse<String> withdraw(String accNo, double amount) {

        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!"ACTIVE".equals(acc.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        if (acc.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        acc.setBalance(acc.getBalance() - amount);
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Amount withdrawn successfully", null);
    }

    // =========================
    // MY ACCOUNTS
    // =========================
    public ApiResponse<List<AccountResponse>> myAccounts(String token) {

        String username = jwtUtil.extractUsername(token);

        List<AccountResponse> list = repo.findByUsername(username)
                .stream()
                .map(a -> new AccountResponse(
                        a.getAccountNumber(),
                        a.getAccountType(),
                        a.getBalance(),
                        a.getStatus()))
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "Accounts fetched", list);
    }

    // =========================
    // GET ACCOUNT
    // =========================
    public ApiResponse<AccountDetailsResponse> getAccount(String token, String accNo) {

        String username = jwtUtil.extractUsername(token);

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!acc.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access");
        }

        return new ApiResponse<>("SUCCESS", "Account details",
                new AccountDetailsResponse(
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getStatus(),
                        acc.getCreatedAt()
                ));
    }

    // =========================
    // GET ALL ACCOUNTS
    // =========================
    public ApiResponse<List<AccountDetailsResponse>> getAllAccounts() {

        List<AccountDetailsResponse> list = repo.findAll()
                .stream()
                .map(a -> new AccountDetailsResponse(
                        a.getAccountNumber(),
                        a.getAccountType(),
                        a.getBalance(),
                        a.getStatus(),
                        a.getCreatedAt()))
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "All accounts", list);
    }

    // =========================
    // BLOCK
    // =========================
    public ApiResponse<String> block(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        acc.setStatus("BLOCKED");
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account blocked", null);
    }

    // =========================
    // UNBLOCK
    // =========================
    public ApiResponse<String> unblock(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        acc.setStatus("ACTIVE");
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account unblocked", null);
    }

    // =========================
    // CLOSE
    // =========================
    public ApiResponse<String> close(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (acc.getBalance() > 0) {
            throw new RuntimeException("Balance must be zero to close account");
        }

        acc.setStatus("CLOSED");
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account closed", null);
    }
}