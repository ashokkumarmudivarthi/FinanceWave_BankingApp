package com.financewave.beneficiary.dto;

public class BeneficiaryResponse {

    private Long id;
    private String accountNumber;
    private String nickname;
    private String bankName;
    private String status;

    public BeneficiaryResponse(Long id, String accountNumber, String nickname,
                               String bankName, String status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.nickname = nickname;
        this.bankName = bankName;
        this.status = status;
    }

    // getters
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getNickname() { return nickname; }
    public String getBankName() { return bankName; }
    public String getStatus() { return status; }
}