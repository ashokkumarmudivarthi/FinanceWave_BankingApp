package com.financewave.transaction.service;

import com.financewave.transaction.dto.*;
import com.financewave.transaction.entity.Transaction;
import com.financewave.transaction.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository repo;

    @Autowired
    private AccountClient accountClient;

    // =========================
    // DEPOSIT
    // =========================
    public ApiResponse<TransactionResponse> deposit(AmountRequest req, String token) {

        validateAmount(req.getAmount());

        boolean isValid = accountClient.validateAccount(req.getAccountNumber(), token);

        if (!isValid) {
            throw new RuntimeException("Invalid or inactive account");
        }

        Transaction tx = buildTransaction(
                null,
                req.getAccountNumber(),
                req.getAmount(),
                "DEPOSIT"
        );

        repo.save(tx);

        return buildResponse("Deposit completed successfully", tx);
    }

    // =========================
    // WITHDRAW
    // =========================
    public ApiResponse<TransactionResponse> withdraw(AmountRequest req, String token) {

        validateAmount(req.getAmount());

        boolean isValid = accountClient.validateAccount(req.getAccountNumber(), token);

        if (!isValid) {
            throw new RuntimeException("Invalid or inactive account");
        }

        Transaction tx = buildTransaction(
                req.getAccountNumber(),
                null,
                req.getAmount(),
                "WITHDRAW"
        );

        repo.save(tx);

        return buildResponse("Withdraw completed successfully", tx);
    }

    // =========================
    // TRANSFER
    // =========================
    public ApiResponse<TransactionResponse> transfer(TransferRequest req, String token) {

        validateAmount(req.getAmount());

        if (req.getFromAccount().equals(req.getToAccount())) {
            throw new RuntimeException("Cannot transfer to same account");
        }

        boolean fromValid = accountClient.validateAccount(req.getFromAccount(), token);
        boolean toValid = accountClient.validateAccount(req.getToAccount(), token);

        if (!fromValid || !toValid) {
            throw new RuntimeException("Invalid or inactive account");
        }

        Transaction tx = buildTransaction(
                req.getFromAccount(),
                req.getToAccount(),
                req.getAmount(),
                "TRANSFER"
        );

        repo.save(tx);

        return buildResponse("Transfer completed successfully", tx);
    }

    // =========================
    // HISTORY
    // =========================
    public ApiResponse<List<TransactionResponse>> history(String accNo, String token) {

        boolean isValid = accountClient.validateAccount(accNo, token);

        if (!isValid) {
            throw new RuntimeException("Invalid account");
        }

        List<TransactionResponse> list = repo
                .findByFromAccountOrToAccount(accNo, accNo)
                .stream()
                .map(tx -> new TransactionResponse(
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus()
                ))
                .collect(Collectors.toList());

        return new ApiResponse<List<TransactionResponse>>(
                "SUCCESS",
                "Transaction history fetched successfully",
                list
        );
    }

    // =========================
    // COMMON METHODS
    // =========================

    private Transaction buildTransaction(String from, String to, double amount, String type) {

        Transaction tx = new Transaction();
        tx.setTransactionId("TXN" + System.currentTimeMillis());
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(LocalDateTime.now());

        return tx;
    }

    private ApiResponse<TransactionResponse> buildResponse(String message, Transaction tx) {

        TransactionResponse response = new TransactionResponse(
                tx.getTransactionId(),
                tx.getType(),
                tx.getAmount(),
                tx.getStatus()
        );

        return new ApiResponse<TransactionResponse>(
                "SUCCESS",
                message,
                response
        );
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
    }
}