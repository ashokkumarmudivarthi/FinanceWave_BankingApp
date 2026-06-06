package com.financewave.loanservice.dto;

import lombok.Data;

public class LoanCustomerRequest {
    public String name;
    public String pan;
    public String aadhaar;
    public String employmentType;
    public double monthlyIncome;
    public int creditScore;
    public String accountNumber;
}