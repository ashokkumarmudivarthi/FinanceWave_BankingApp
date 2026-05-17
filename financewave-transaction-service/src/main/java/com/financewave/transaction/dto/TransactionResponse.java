package com.financewave.transaction.dto;

public class TransactionResponse {

    private String transactionId;
    private String type;
    private double amount;
    private String status;

    // ✅ REQUIRED
    public TransactionResponse(String transactionId, String type, double amount, String status) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.status = status;
    }

    // getters
    public String getTransactionId() { return transactionId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}