package com.financewave.transaction.service;

import com.financewave.transaction.dto.*;
import com.financewave.transaction.entity.AuditLog;
import com.financewave.transaction.entity.Transaction;
import com.financewave.transaction.repository.AuditLogRepository;
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

    @Autowired
    private AuditLogRepository auditRepo;

    // =========================
    // DEPOSIT
    // =========================
    public ApiResponse<TransactionResponse> deposit(AmountRequest req, String token) {

        Transaction tx = buildTransaction(null, req.getAccountNumber(), req.getAmount(), "DEPOSIT");

        try {
            validateAmount(req.getAmount());

            if (!accountClient.validateAccount(req.getAccountNumber(), token)) {
                throw new RuntimeException("Invalid or inactive account");
            }

            // 🔥 WILL THROW if failed
            accountClient.deposit(req.getAccountNumber(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit("USER", "DEPOSIT", "SUCCESS",
                    "Deposited " + req.getAmount() + " to " + req.getAccountNumber());

            return buildResponse("Deposit completed successfully", tx);

        } catch (Exception ex) {

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit("USER", "DEPOSIT", "FAILED", ex.getMessage());

            throw new RuntimeException(ex.getMessage());
        }
    }

    // =========================
    // WITHDRAW
    // =========================
    public ApiResponse<TransactionResponse> withdraw(AmountRequest req, String token) {

        Transaction tx = buildTransaction(req.getAccountNumber(), null, req.getAmount(), "WITHDRAW");

        try {
            validateAmount(req.getAmount());

            if (!accountClient.validateAccount(req.getAccountNumber(), token)) {
                throw new RuntimeException("Invalid or inactive account");
            }

            // 🔥 FIX: NO BOOLEAN
            accountClient.withdraw(req.getAccountNumber(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit("USER", "WITHDRAW", "SUCCESS",
                    "Withdrawn " + req.getAmount() + " from " + req.getAccountNumber());

            return buildResponse("Withdraw completed successfully", tx);

        } catch (Exception ex) {

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit("USER", "WITHDRAW", "FAILED", ex.getMessage());

            throw new RuntimeException(ex.getMessage());
        }
    }

    // =========================
    // TRANSFER (WITH ROLLBACK)
    // =========================
    public ApiResponse<TransactionResponse> transfer(TransferRequest req, String token) {

        Transaction tx = buildTransaction(
                req.getFromAccount(),
                req.getToAccount(),
                req.getAmount(),
                "TRANSFER"
        );

        boolean debited = false;

        try {
            validateAmount(req.getAmount());

            if (req.getFromAccount().equals(req.getToAccount())) {
                throw new RuntimeException("Cannot transfer to same account");
            }

            if (!accountClient.validateAccount(req.getFromAccount(), token) ||
                !accountClient.validateAccount(req.getToAccount(), token)) {
                throw new RuntimeException("Invalid or inactive account");
            }

            // STEP 1
            accountClient.withdraw(req.getFromAccount(), req.getAmount(), token);
            debited = true;

            // STEP 2
            accountClient.deposit(req.getToAccount(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit("USER", "TRANSFER", "SUCCESS",
                    "Transferred " + req.getAmount() +
                    " from " + req.getFromAccount() +
                    " to " + req.getToAccount());

            return buildResponse("Transfer completed successfully", tx);

        } catch (Exception ex) {

            // 🔁 ROLLBACK
            if (debited) {
                try {
                    accountClient.deposit(req.getFromAccount(), req.getAmount(), token);
                } catch (Exception rollbackEx) {
                    System.out.println("CRITICAL: Rollback failed");
                }
            }

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit("USER", "TRANSFER", "FAILED", ex.getMessage());

            throw new RuntimeException("Transfer failed: " + ex.getMessage());
        }
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

        return new ApiResponse<>("SUCCESS", "Transaction history fetched", list);
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

    private void logAudit(String username, String action, String status, String details) {

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setStatus(status);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());

        auditRepo.save(log);
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
    }
}