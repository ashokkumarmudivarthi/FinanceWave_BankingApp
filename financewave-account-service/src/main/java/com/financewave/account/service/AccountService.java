package com.financewave.account.service;

import com.financewave.account.dto.*;
import com.financewave.account.entity.Account;
import com.financewave.account.repository.AccountRepository;
import com.financewave.account.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    // GET ACCOUNT DETAILS
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
    // ADMIN - GET ALL ACCOUNTS
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
    // BLOCK ACCOUNT
    // =========================
    public ApiResponse<String> block(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        acc.setStatus("BLOCKED");
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account blocked", null);
    }

    // =========================
    // UNBLOCK ACCOUNT
    // =========================
    public ApiResponse<String> unblock(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        acc.setStatus("ACTIVE");
        repo.save(acc);

        return new ApiResponse<>("SUCCESS", "Account unblocked", null);
    }

    // =========================
    // CLOSE ACCOUNT
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

    // =========================
    // VALIDATE ACCOUNT (INTERNAL)
    // =========================
    public boolean validateAccount(String accNo) {

        Account acc = repo.findByAccountNumber(accNo)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return "ACTIVE".equals(acc.getStatus());
    }
}