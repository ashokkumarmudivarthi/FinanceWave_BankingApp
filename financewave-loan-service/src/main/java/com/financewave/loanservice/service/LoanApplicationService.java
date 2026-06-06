package com.financewave.loanservice.service;

import com.financewave.loanservice.dto.ApiResponse;
import com.financewave.loanservice.dto.LoanApplicationRequest;
import com.financewave.loanservice.entity.LoanApplication;
import com.financewave.loanservice.entity.LoanCustomer;
import com.financewave.loanservice.entity.LoanEmi;
import com.financewave.loanservice.exception.ResourceNotFoundException;
import com.financewave.loanservice.repository.LoanApplicationRepository;
import com.financewave.loanservice.repository.LoanCustomerRepository;
import com.financewave.loanservice.repository.LoanEmiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository repo;

    @Autowired
    private LoanCustomerRepository customerRepo;

    @Autowired
    private LoanEmiRepository emiRepo;

    // =========================
    // INTEREST RATE
    // =========================
    private double getRate(String type) {
        return switch (type) {
            case "PERSONAL" -> 12.5;
            case "HOME" -> 8.5;
            case "CAR" -> 9.5;
            case "TW" -> 11;
            case "BUSINESS" -> 14;
            default -> throw new RuntimeException("Invalid loan type");
        };
    }

    // =========================
    // EMI CALCULATION
    // =========================
    private double calculateEMI(double amount, int months, double rate) {

        double r = rate / 12 / 100;

        return (amount * r * Math.pow(1 + r, months)) /
                (Math.pow(1 + r, months) - 1);
    }

    // =========================
    // APPLY LOAN
    // =========================
    public ApiResponse<?> apply(LoanApplicationRequest req) {

        LoanCustomer c = customerRepo.findById(req.customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (c.getCreditScore() < 650)
            throw new RuntimeException("Low credit score");

        double rate = getRate(req.loanType);
        double emi = calculateEMI(req.loanAmount, req.tenureMonths, rate);

        LoanApplication loan = new LoanApplication();

        loan.setApplicationId("FW-LOAN-" + System.currentTimeMillis());
        loan.setCustomerId(c.getId());
        loan.setLoanType(req.loanType);
        loan.setLoanAmount(req.loanAmount);
        loan.setTenureMonths(req.tenureMonths);
        loan.setInterestRate(rate);
        loan.setEmi(emi);
        loan.setStatus("APPLIED");
        loan.setCreatedAt(LocalDateTime.now());

        repo.save(loan);

        return new ApiResponse<>("SUCCESS", "Loan applied successfully", loan);
    }

    // =========================
    // CHECK STATUS
    // =========================
    public ApiResponse<?> getStatus(String appId) {

        // ✅ STEP 1: FORMAT VALIDATION (ADD HERE)
        if (!appId.startsWith("FW-LOAN-")) {
            throw new IllegalArgumentException("Invalid application ID format");
        }

        // ✅ STEP 2: FETCH FROM DB
        LoanApplication loan = repo.findByApplicationId(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found"));

        // ✅ STEP 3: RETURN RESPONSE
        return new ApiResponse<>("SUCCESS", "Status fetched", loan);
    }

    // =========================
    // APPROVE LOAN
    // =========================
    public ApiResponse<?> approve(Long id) {

        LoanApplication loan = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("APPROVED");
        repo.save(loan);

        generateEmiSchedule(loan);

        return new ApiResponse<>("SUCCESS", "Loan approved", null);
    }

    // =========================
    // EMI GENERATION
    // =========================
    private void generateEmiSchedule(LoanApplication loan) {

        for (int i = 1; i <= loan.getTenureMonths(); i++) {

            LoanEmi emi = new LoanEmi();

            emi.setLoanId(loan.getId());
            emi.setAmount(loan.getEmi());
            emi.setStatus("PENDING");
            emi.setDueDate(LocalDate.now().plusMonths(i));

            emiRepo.save(emi);
        }
    }
    
    public ApiResponse<?> getEmiSchedule(Long loanId) {

        // ✅ Check loan exists
        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // ✅ Fetch EMI list
        List<LoanEmi> emis = emiRepo.findByLoanId(loanId);

        if (emis.isEmpty()) {
            throw new ResourceNotFoundException("No EMI schedule found");
        }

        return new ApiResponse<>("SUCCESS", "EMI schedule fetched", emis);
    }
}