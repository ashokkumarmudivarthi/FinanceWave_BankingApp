package com.financewave.auth.controller;

import com.financewave.auth.dto.ApiResponse;
import com.financewave.auth.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AuthService authService;

    @GetMapping("/audit")
    public ApiResponse<Object> getAudit(@RequestParam String username) {

        Object data = authService.getAuditLogs(username);

        return new ApiResponse<>("SUCCESS", "Audit logs fetched", data);
    }
}