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

        // ✅ DEBUG (VERY IMPORTANT)
        System.out.println("Initial Balance: " + req.getInitialBalance());

        // ✅ FIXED FIELD NAME
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

        return new ApiResponse<>(
                "SUCCESS",
                "Account created",
                new AccountResponse(
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getStatus()
                )
        );
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
}