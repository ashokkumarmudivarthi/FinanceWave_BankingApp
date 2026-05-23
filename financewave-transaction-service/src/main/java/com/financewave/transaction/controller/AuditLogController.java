package com.financewave.transaction.controller;

import com.financewave.transaction.dto.ApiResponse;
import com.financewave.transaction.entity.AuditLog;
import com.financewave.transaction.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions/audit")
public class AuditLogController {

    @Autowired
    private AuditLogRepository repo;

    // =========================
    // GET ALL LOGS
    // =========================
    @GetMapping
    public ApiResponse<List<AuditLog>> getAllLogs() {

        List<AuditLog> logs = repo.findAll();

        return new ApiResponse<>(
                "SUCCESS",
                "Audit logs fetched",
                logs
        );
    }

    // =========================
    // GET BY USERNAME
    // =========================
    @GetMapping("/{username}")
    public ApiResponse<List<AuditLog>> getByUser(@PathVariable String username) {

        List<AuditLog> logs = repo.findByUsername(username);

        return new ApiResponse<>(
                "SUCCESS",
                "User audit logs fetched",
                logs
        );
    }
}