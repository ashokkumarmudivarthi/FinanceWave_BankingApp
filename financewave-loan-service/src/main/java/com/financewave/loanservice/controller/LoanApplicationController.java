package com.financewave.loanservice.controller;

import com.financewave.loanservice.dto.ApiResponse;
import com.financewave.loanservice.dto.LoanApplicationRequest;
import com.financewave.loanservice.entity.LoanApplication;
import com.financewave.loanservice.service.LoanApplicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan/applications")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService service;

    @PostMapping("/apply")
    public ApiResponse<?> apply(@RequestBody LoanApplicationRequest req) {
        return service.apply(req);
    }

    @GetMapping("/status/{appId}")
    public ApiResponse<?> status(@PathVariable String appId) {
        return service.getStatus(appId);
    }

    @PostMapping("/approve/{id}")
    public ApiResponse<?> approve(@PathVariable Long id) {
        return service.approve(id);
    }
    
    @GetMapping("/emi/{loanId}")
    public ApiResponse<?> getEmi(@PathVariable Long loanId) {
        return service.getEmiSchedule(loanId);
    }
}