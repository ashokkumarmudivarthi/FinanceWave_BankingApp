package com.financewave.transaction.repository;

import com.financewave.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromAccountOrToAccount(String from, String to);
    Optional<Transaction> findByTransactionId(String transactionId);
}