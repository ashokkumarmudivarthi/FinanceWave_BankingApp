package com.financewave.beneficiary.controller;

import com.financewave.beneficiary.dto.*;
import com.financewave.beneficiary.service.BeneficiaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService service;

    // =========================
    // ADD
    // =========================
    @PostMapping("/add")
    public ApiResponse<BeneficiaryResponse> add(
            @RequestHeader("Authorization") String header,
            @RequestBody AddBeneficiaryRequest req) {

        return service.add(header.substring(7), req);
    }

    // =========================
    // MY LIST
    // =========================
    @GetMapping("/my")
    public ApiResponse<?> my(
            @RequestHeader("Authorization") String header) {

        return service.myList(header.substring(7));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {

        return service.delete(header.substring(7), id);
    }

    // =========================
    // ADMIN: PENDING
    // =========================
    @GetMapping("/pending")
    public ApiResponse<?> getPending(
            @RequestHeader("Authorization") String header) {

        return service.getPending(header.substring(7));
    }

    // =========================
    // ADMIN: APPROVE
    // =========================
    @PostMapping("/approve/{id}")
    public ApiResponse<String> approve(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id) {

        return service.approve(header.substring(7), id);
    }

    // =========================
    // ADMIN: REJECT
    // =========================
    @PostMapping("/reject/{id}")
    public ApiResponse<String> reject(
            @RequestHeader("Authorization") String header,
            @PathVariable Long id,
            @RequestParam String reason) {

        return service.reject(header.substring(7), id, reason);
    }

    // =========================
    // ADMIN: ALL
    // =========================
    @GetMapping("/all")
    public ApiResponse<?> all(
            @RequestHeader("Authorization") String header) {

        return service.getAll(header.substring(7));
    }

    // =========================
    // VALIDATE (FOR TRANSACTION SERVICE)
    // =========================
    @GetMapping("/validate")
    public boolean validate(
            @RequestHeader("Authorization") String header,
            @RequestParam String account) {

        return service.validate(header.substring(7), account);
    }
}