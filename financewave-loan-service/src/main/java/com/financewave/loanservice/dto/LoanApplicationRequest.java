package com.financewave.loanservice.dto;

import lombok.Data;

@Data
public class LoanApplicationRequest {
    public Long customerId;
    public String loanType;
    public double loanAmount;
    public int tenureMonths;
}