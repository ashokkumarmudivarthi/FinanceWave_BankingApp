package com.financewave.beneficiary.dto;

public class AddBeneficiaryRequest {

    private String accountNumber;
    private String nickname;
    private String bankName;
    private String ifsc;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }
}