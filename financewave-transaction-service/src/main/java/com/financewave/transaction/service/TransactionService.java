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

        if (!accountClient.validateAccount(req.getAccountNumber(), token)) {
            throw new RuntimeException("Invalid or inactive account");
        }

        // ✅ CALL ACCOUNT SERVICE
        boolean success = accountClient.deposit(
                req.getAccountNumber(),
                req.getAmount(),
                token
        );

        if (!success) {
            throw new RuntimeException("Deposit failed");
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

        if (!accountClient.validateAccount(req.getAccountNumber(), token)) {
            throw new RuntimeException("Invalid or inactive account");
        }

        // ✅ CALL ACCOUNT SERVICE
        boolean success = accountClient.withdraw(
                req.getAccountNumber(),
                req.getAmount(),
                token
        );

        if (!success) {
            throw new RuntimeException("Insufficient balance or withdraw failed");
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
    // TRANSFER (WITH SAFE ROLLBACK)
    // =========================
    public ApiResponse<TransactionResponse> transfer(TransferRequest req, String token) {

        validateAmount(req.getAmount());

        if (req.getFromAccount().equals(req.getToAccount())) {
            throw new RuntimeException("Cannot transfer to same account");
        }

        if (!accountClient.validateAccount(req.getFromAccount(), token) ||
            !accountClient.validateAccount(req.getToAccount(), token)) {
            throw new RuntimeException("Invalid or inactive account");
        }

        boolean withdrawSuccess = false;

        try {

            // =========================
            // STEP 1: WITHDRAW
            // =========================
            withdrawSuccess = accountClient.withdraw(
                    req.getFromAccount(),
                    req.getAmount(),
                    token
            );

            if (!withdrawSuccess) {
                throw new RuntimeException("Insufficient balance");
            }

            // =========================
            // STEP 2: DEPOSIT
            // =========================
            boolean depositSuccess = accountClient.deposit(
                    req.getToAccount(),
                    req.getAmount(),
                    token
            );

            if (!depositSuccess) {
                throw new RuntimeException("Deposit failed");
            }

        } catch (Exception ex) {

            // =========================
            // ROLLBACK LOGIC
            // =========================
            if (withdrawSuccess) {
                try {
                    accountClient.deposit(
                            req.getFromAccount(),
                            req.getAmount(),
                            token
                    );
                    System.out.println("Rollback SUCCESS");
                } catch (Exception rollbackEx) {
                    System.out.println("CRITICAL: Rollback FAILED");
                }
            }

            throw new RuntimeException("Transaction failed. Amount reversed if debited.");
        }

        // =========================
        // SAVE TRANSACTION ONLY AFTER SUCCESS
        // =========================
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

        if (!accountClient.validateAccount(accNo, token)) {
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

        return new ApiResponse<>(
                "SUCCESS",
                "Transaction history fetched",
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

        return new ApiResponse<>(
                "SUCCESS",
                message,
                new TransactionResponse(
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus()
                )
        );
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
    }
}