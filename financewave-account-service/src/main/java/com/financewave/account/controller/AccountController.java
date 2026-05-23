package com.financewave.account.controller;

import com.financewave.account.dto.*;
import com.financewave.account.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    // =========================
    // CREATE
    // =========================
    @PostMapping("/create")
    public ApiResponse<AccountResponse> create(
            @RequestHeader("Authorization") String header,
            @RequestBody CreateAccountRequest req) {

        return service.create(header.substring(7), req);
    }

    // =========================
    // MY ACCOUNTS
    // =========================
    @GetMapping("/my")
    public ApiResponse<?> myAccounts(
            @RequestHeader("Authorization") String header) {

        return service.myAccounts(header.substring(7));
    }

    // =========================
    // ACCOUNT DETAILS
    // =========================
    @GetMapping("/{accNo}")
    public ApiResponse<?> getAccount(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo) {

        return service.getAccount(header.substring(7), accNo);
    }

    // =========================
    // ADMIN
    // =========================
    @GetMapping("/all")
    public ApiResponse<?> allAccounts() {
        return service.getAllAccounts();
    }

    // =========================
    // ACCOUNT ACTIONS
    // =========================
    @PostMapping("/block")
    public ApiResponse<String> block(@RequestBody AccountActionRequest req) {
        return service.block(req.getAccountNumber());
    }

    @PostMapping("/unblock")
    public ApiResponse<String> unblock(@RequestBody AccountActionRequest req) {
        return service.unblock(req.getAccountNumber());
    }

    @PostMapping("/close")
    public ApiResponse<String> close(@RequestBody AccountActionRequest req) {
        return service.close(req.getAccountNumber());
    }

    // =========================
    // VALIDATE (INTERNAL)
    // =========================
    @GetMapping("/validate/{accNo}")
    public boolean validate(@PathVariable String accNo) {
        return service.validateAccount(accNo);
    }

    // =========================
    // DEPOSIT (INTERNAL - SHOULD BE SECURED)
    // =========================
    @PutMapping("/deposit/{accountNumber}")
    public ApiResponse<String> deposit(
            @PathVariable String accountNumber,
            @RequestParam double amount) {

        return service.deposit(accountNumber, amount);
    }

    // =========================
    // WITHDRAW (INTERNAL - SHOULD BE SECURED)
    // =========================
    @PutMapping("/withdraw/{accountNumber}")
    public ApiResponse<String> withdraw(
            @PathVariable String accountNumber,
            @RequestParam double amount) {

        return service.withdraw(accountNumber, amount);
    }
}