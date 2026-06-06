package com.financewave.loanservice.service;

import com.financewave.loanservice.dto.ApiResponse;
import com.financewave.loanservice.dto.LoanCustomerRequest;
import com.financewave.loanservice.entity.LoanCustomer;
import com.financewave.loanservice.repository.LoanCustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanCustomerService {

    @Autowired
    private LoanCustomerRepository repo;

    public ApiResponse<?> register(LoanCustomerRequest req) {

        LoanCustomer c = new LoanCustomer();

        c.setRegistrationId("FW-CUST-" + System.currentTimeMillis());
        c.setName(req.name);
        c.setPan(req.pan);
        c.setAadhaar(req.aadhaar);
        c.setEmploymentType(req.employmentType);
        c.setMonthlyIncome(req.monthlyIncome);
        c.setCreditScore(req.creditScore);
        c.setAccountNumber(req.accountNumber);

        repo.save(c);

        return new ApiResponse<>("SUCCESS", "Customer registered", c);
    }
}