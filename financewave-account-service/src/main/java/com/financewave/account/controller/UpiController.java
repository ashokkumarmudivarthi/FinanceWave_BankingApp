package com.financewave.account.controller;

import com.financewave.account.service.UpiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/upi")
public class UpiController {

    @Autowired
    private UpiService service;

    // =========================
    // CREATE VPA
    // =========================
    @PostMapping("/create")
    public String create(
            @RequestHeader("Authorization") String header,
            @RequestParam String vpa,
            @RequestParam String account) {

        return service.create(header.substring(7), vpa, account);
    }

    // =========================
    // VALIDATE VPA
    // =========================
    @GetMapping("/validate")
    public boolean validate(@RequestParam String vpa) {
        return service.validate(vpa);
    }

    // =========================
    // GET ACCOUNT
    // =========================
    @GetMapping("/account")
    public String getAccount(@RequestParam String vpa) {
        return service.getAccount(vpa);
    }
}