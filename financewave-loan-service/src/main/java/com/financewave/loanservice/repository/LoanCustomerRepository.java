package com.financewave.loanservice.repository;

import com.financewave.loanservice.entity.LoanCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanCustomerRepository extends JpaRepository<LoanCustomer, Long> {
}