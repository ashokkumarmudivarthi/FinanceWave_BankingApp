package com.financewave.loanservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class LoanEmi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loanId;
    private double amount;
    private String status;
    private LocalDate dueDate;

    // GETTERS & SETTERS

    public Long getId() { return id; }

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}