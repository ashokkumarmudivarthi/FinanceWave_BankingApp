package com.financewave.transaction.repository;

import com.financewave.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    Optional<Transaction> findByTransactionId(String transactionId);
    // ✅ REQUIRED FOR STATUS API
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
    	       "WHERE t.fromAccount = :acc " +
    	       "AND t.type = 'WITHDRAW' " +
    	       "AND t.createdAt >= :startOfDay " +
    	       "AND t.createdAt < :endOfDay")
    	double getTodayDebitTotal(
    	        @Param("acc") String acc,
    	        @Param("startOfDay") LocalDateTime startOfDay,
    	        @Param("endOfDay") LocalDateTime endOfDay
    	);
}