package com.financewave.loanservice.controller;

import com.financewave.loanservice.dto.ApiResponse;
import com.financewave.loanservice.dto.LoanCustomerRequest;
import com.financewave.loanservice.service.LoanCustomerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan/customers")
public class LoanCustomerController {

    @Autowired
    private LoanCustomerService service;

    @PostMapping(value = "/register", produces = "application/json")
    public ApiResponse<?> register(@RequestBody LoanCustomerRequest req) {
        return service.register(req);
    }
}