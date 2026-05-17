package com.financewave.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;
    private String accountType;
    private double balance;
    private String status;
}