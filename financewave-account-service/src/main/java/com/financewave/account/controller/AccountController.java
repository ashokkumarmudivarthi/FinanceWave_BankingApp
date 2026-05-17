package com.financewave.account.controller;

import com.financewave.account.dto.*;
import com.financewave.account.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    @PostMapping("/create")
    public ApiResponse<AccountResponse> create(
            @RequestHeader("Authorization") String header,
            @RequestBody CreateAccountRequest req) {

        return service.create(header.substring(7), req);
    }

    @GetMapping("/my")
    public ApiResponse<?> myAccounts(
            @RequestHeader("Authorization") String header) {

        return service.myAccounts(header.substring(7));
    }
}