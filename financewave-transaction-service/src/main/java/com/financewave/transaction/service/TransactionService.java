package com.financewave.transaction.service;

import com.financewave.transaction.client.BeneficiaryClient;
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

    @Autowired
    private BeneficiaryClient beneficiaryClient;

    // =========================
    // DEPOSIT
    // =========================
    public ApiResponse<TransactionResponse> deposit(AmountRequest req, String token) {

        Transaction tx = buildTransaction(null, req.getAccountNumber(), req.getAmount(), "DEPOSIT");

        try {
            validateAmount(req.getAmount());

            if (!accountClient.validateAccount(req.getAccountNumber(), token)) {
                throw new RuntimeException("Invalid account");
            }

            accountClient.deposit(req.getAccountNumber(), req.getAmount(), token);

            tx.setStatus("SUCCESS");
            repo.save(tx);

            return buildResponse("Deposit success", tx);

        } catch (Exception ex) {
            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

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

            return buildResponse("Withdraw success", tx);

        } catch (Exception ex) {
            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            throw new RuntimeException(ex.getMessage());
        }
    }

    // =========================
    // TRANSFER (FINAL FIXED)
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

            // ✅ BENEFICIARY VALIDATION
            boolean isValid = beneficiaryClient.validate(token, req.getToAccount());

            if (!isValid) {
                throw new RuntimeException("Beneficiary not approved");
            }

            // ✅ SOURCE ACCOUNT
            if (!accountClient.validateAccount(req.getFromAccount(), token)) {
                throw new RuntimeException("Invalid source account");
            }

            // ✅ INTERNAL / EXTERNAL
            boolean isInternal = req.getToAccount().startsWith("FW");

            if (isInternal) {
                if (!accountClient.validateAccount(req.getToAccount(), token)) {
                    throw new RuntimeException("Target account not found");
                }
            }

            fraudCheck(req.getFromAccount(), req.getAmount());

            // STEP 1
            accountClient.withdraw(req.getFromAccount(), req.getAmount(), token);
            debited = true;

            // STEP 2
            if (isInternal) {
                accountClient.deposit(req.getToAccount(), req.getAmount(), token);
            }

            tx.setStatus("SUCCESS");
            repo.save(tx);

            return buildResponse("Transfer success", tx);

        } catch (Exception ex) {

            if (debited) {
                try {
                    accountClient.deposit(req.getFromAccount(), req.getAmount(), token);
                } catch (Exception ignore) {}
            }

            tx.setStatus("FAILED");
            tx.setFailureReason(ex.getMessage());
            repo.save(tx);

            throw new RuntimeException("Transfer failed: " + ex.getMessage());
        }
    }

    // =========================
    // HISTORY
    // =========================
    public ApiResponse<List<TransactionResponse>> history(String accNo, String token) {

        List<TransactionResponse> list = repo
                .findByFromAccountOrToAccount(accNo, accNo)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "History fetched", list);
    }

    // =========================
    // MINI
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
    // STATEMENT
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

        return new ApiResponse<>("SUCCESS", "Statement", list);
    }

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

    private void validateAmount(double amount) {
        if (amount <= 0) throw new RuntimeException("Invalid amount");
    }

    private void fraudCheck(String accNo, double amount) {

        if (!featureToggle.isFraudCheckEnabled()) return;

        // ✅ calculate today's range
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        double today = repo.getTodayDebitTotal(accNo, start, end);

        if ((today + amount) > 200000) {
            throw new RuntimeException("Daily limit exceeded");
        }

        if (amount > 100000) {
            throw new RuntimeException("Exceeds per transaction limit");
        }
    }
 // =========================
 // GET TRANSACTION STATUS
 // =========================
 public ApiResponse<TransactionResponse> getTransaction(String txnId) {

     Transaction tx = repo.findByTransactionId(txnId)
             .orElseThrow(() -> new RuntimeException("Transaction not found"));

     return buildResponse("Transaction fetched", tx);
 }
}