package com.financewave.transaction.service;

import com.financewave.transaction.config.FeatureToggleConfig;
import com.financewave.transaction.dto.*;
import com.financewave.transaction.entity.AuditLog;
import com.financewave.transaction.entity.Transaction;
import com.financewave.transaction.repository.AuditLogRepository;
import com.financewave.transaction.repository.TransactionRepository;
import com.financewave.transaction.security.JwtUtil;

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

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private FeatureToggleConfig featureToggle;

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

            // ✅ STEP 1: FRAUD CHECK (BEFORE MONEY)
            //fraudCheck(req.getAccountNumber(), req.getAmount());

            // ✅ STEP 2: ACTUAL TRANSACTION
            accountClient.deposit(req.getAccountNumber(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit(getUsername(token), "DEPOSIT", "SUCCESS",
                    "Deposited " + req.getAmount());

            return buildResponse("Deposit completed successfully", tx);

        } catch (Exception ex) {

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit(getUsername(token), "DEPOSIT", "FAILED", ex.getMessage());

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
                throw new RuntimeException("Invalid account");
            }
            
            fraudCheck(req.getAccountNumber(), req.getAmount());

            accountClient.withdraw(req.getAccountNumber(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit(getUsername(token), "WITHDRAW", "SUCCESS", "Withdraw success");

            return buildResponse("Withdraw completed successfully", tx);

        } catch (Exception ex) {

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit(getUsername(token), "WITHDRAW", "FAILED", ex.getMessage());

            throw new RuntimeException(ex.getMessage());
        }
    }

    // =========================
    // TRANSFER
    // =========================
    public ApiResponse<TransactionResponse> transfer(TransferRequest req, String token) {

        Transaction tx = buildTransaction(req.getFromAccount(), req.getToAccount(), req.getAmount(), "TRANSFER");

        boolean debited = false;

        try {
            validateAmount(req.getAmount());

            if (req.getFromAccount().equals(req.getToAccount())) {
                throw new RuntimeException("Cannot transfer to same account");
            }
            
            fraudCheck(req.getFromAccount(), req.getAmount());

            accountClient.withdraw(req.getFromAccount(), req.getAmount(), token);
            debited = true;

            accountClient.deposit(req.getToAccount(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            logAudit(getUsername(token), "TRANSFER", "SUCCESS", "Transfer success");

            return buildResponse("Transfer completed successfully", tx);

        } catch (Exception ex) {

            if (debited) {
                try {
                    accountClient.deposit(req.getFromAccount(), req.getAmount(), token);
                } catch (Exception ignore) {}
            }

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            logAudit(getUsername(token), "TRANSFER", "FAILED", ex.getMessage());

            throw new RuntimeException("Transfer failed: " + ex.getMessage());
        }
    }

    // =========================
    // GET TXN STATUS (FIXED)
    // =========================
    public ApiResponse<TransactionResponse> getTransaction(String txnId) {

        Transaction tx = repo.findByTransactionId(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return buildResponse("Transaction fetched", tx);
    }

    // =========================
    // MINI STATEMENT
    // =========================
    public ApiResponse<List<TransactionResponse>> miniStatement(String accNo, String token) {

        List<TransactionResponse> list = repo
                .findTop5ByFromAccountOrToAccountOrderByCreatedAtDesc(accNo, accNo)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "Mini statement", list);
    }

    // =========================
    // DATE RANGE STATEMENT
    // =========================
    public ApiResponse<List<TransactionResponse>> statement(
            String accNo,
            LocalDateTime start,
            LocalDateTime end,
            String token) {

        List<TransactionResponse> list = repo
                .findByCreatedAtBetweenAndFromAccountOrToAccount(start, end, accNo, accNo)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "Statement fetched", list);
    }

    // =========================
    // SHORTCUTS
    // =========================
    public ApiResponse<List<TransactionResponse>> lastMonth(String accNo, String token) {
        return statement(accNo, LocalDateTime.now().minusMonths(1), LocalDateTime.now(), token);
    }

    public ApiResponse<List<TransactionResponse>> last3Months(String accNo, String token) {
        return statement(accNo, LocalDateTime.now().minusMonths(3), LocalDateTime.now(), token);
    }

    public ApiResponse<List<TransactionResponse>> last6Months(String accNo, String token) {
        return statement(accNo, LocalDateTime.now().minusMonths(6), LocalDateTime.now(), token);
    }

    // =========================
    // HELPERS
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

    private TransactionResponse map(Transaction tx) {
        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getType(),
                tx.getAmount(),
                tx.getStatus()
        );
    }

    private ApiResponse<TransactionResponse> buildResponse(String msg, Transaction tx) {
        return new ApiResponse<>("SUCCESS", msg, map(tx));
    }

    private void logAudit(String user, String action, String status, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(user);
        log.setAction(action);
        log.setStatus(status);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        auditRepo.save(log);
    }

    private void validateAmount(double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be greater than zero");
    }

    private String getUsername(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    
    public ApiResponse<List<TransactionResponse>> history(String accNo, String token) {

        // ✅ SECURITY CHECK
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
    
    //=============Fraud Check==================
    
    private void fraudCheck(String accNo, double amount) {

        if (!featureToggle.isFraudCheckEnabled()) return;

        // ✅ Rule 1: Single txn limit
        if (amount > 100000) {
            throw new RuntimeException("Transaction blocked: exceeds single transaction limit");
        }

        // ✅ Rule 2: Daily debit limit ONLY
        double todayTotal = repo.getTodayDebitTotal(accNo);

        if ((todayTotal + amount) > 200000) {
            throw new RuntimeException("Daily transaction limit exceeded");
        }
    }
}