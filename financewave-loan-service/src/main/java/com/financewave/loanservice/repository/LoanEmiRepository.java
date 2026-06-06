package com.financewave.loanservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financewave.loanservice.entity.LoanEmi;

public interface LoanEmiRepository extends JpaRepository<LoanEmi, Long> {
    List<LoanEmi> findByLoanId(Long loanId);
}