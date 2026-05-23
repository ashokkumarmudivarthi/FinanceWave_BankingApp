package com.financewave.transaction.repository;

import com.financewave.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountOrToAccount(String from, String to);

    // ✅ FIXED MINI STATEMENT
    List<Transaction> findTop5ByFromAccountOrToAccountOrderByCreatedAtDesc(String from, String to);

    // ✅ FIXED DATE RANGE
    List<Transaction> findByCreatedAtBetweenAndFromAccountOrToAccount(
            LocalDateTime start,
            LocalDateTime end,
            String from,
            String to
    );

    // ✅ REQUIRED FOR STATUS API
    Optional<Transaction> findByTransactionId(String transactionId);
}