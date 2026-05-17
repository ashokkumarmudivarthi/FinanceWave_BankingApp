package com.financewave.transaction.controller;

import com.financewave.transaction.dto.*;
import com.financewave.transaction.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService service;

    // =========================
    // DEPOSIT
    // =========================
    @PostMapping("/deposit")
    public ApiResponse<TransactionResponse> deposit(
            @RequestHeader("Authorization") String header,
            @RequestBody AmountRequest req) {

        return service.deposit(req, header.substring(7));
    }

    // =========================
    // WITHDRAW
    // =========================
    @PostMapping("/withdraw")
    public ApiResponse<TransactionResponse> withdraw(
            @RequestHeader("Authorization") String header,
            @RequestBody AmountRequest req) {

        return service.withdraw(req, header.substring(7));
    }

    // =========================
    // TRANSFER
    // =========================
    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(
            @RequestHeader("Authorization") String header,
            @RequestBody TransferRequest req) {

        return service.transfer(req, header.substring(7));
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================
    @GetMapping("/{accountNumber}")
    public ApiResponse<List<TransactionResponse>> history(
            @RequestHeader("Authorization") String header,
            @PathVariable String accountNumber) {

        return service.history(accountNumber, header.substring(7));
    }
}