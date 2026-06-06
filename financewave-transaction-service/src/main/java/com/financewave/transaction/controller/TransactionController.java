package com.financewave.transaction.controller;

import com.financewave.transaction.dto.*;
import com.financewave.transaction.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    // =========================
    // TRANSACTION STATUS
    // =========================
    @GetMapping("/status/{txnId}")
    public ApiResponse<TransactionResponse> getTransaction(
            @PathVariable String txnId) {

        return service.getTransaction(txnId);
    }

    // =========================
    // MINI STATEMENT
    // =========================
    @GetMapping("/mini/{accNo}")
    public ApiResponse<List<TransactionResponse>> miniStatement(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo) {

        return service.miniStatement(accNo, header.substring(7));
    }

    // =========================
    // CUSTOM STATEMENT
    // =========================
    @GetMapping("/statement/{accNo}")
    public ApiResponse<List<TransactionResponse>> statement(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo,
            @RequestParam String start,
            @RequestParam String end) {

        return service.statement(
                accNo,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end),
                header.substring(7)
        );
    }

    // =========================
    // LAST MONTH
    // =========================
    @GetMapping("/last-month/{accNo}")
    public ApiResponse<List<TransactionResponse>> lastMonth(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo) {

        return service.lastMonth(accNo, header.substring(7));
    }

    // =========================
    // LAST 3 MONTHS
    // =========================
    @GetMapping("/last-3months/{accNo}")
    public ApiResponse<List<TransactionResponse>> last3Months(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo) {

        return service.last3Months(accNo, header.substring(7));
    }

    // =========================
    // LAST 6 MONTHS
    // =========================
    @GetMapping("/last-6months/{accNo}")
    public ApiResponse<List<TransactionResponse>> last6Months(
            @RequestHeader("Authorization") String header,
            @PathVariable String accNo) {

        return service.last6Months(accNo, header.substring(7));
    }
    @PostMapping("/upi")
    public ApiResponse<TransactionResponse> upi(
            @RequestHeader("Authorization") String header,
            @RequestParam String fromAccount,
            @RequestParam String vpa,
            @RequestParam double amount) {

        return service.upiTransfer(
                header.substring(7),
                fromAccount,
                vpa,
                amount
        );
    }
}