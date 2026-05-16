package com.financewave.auth.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/secure")
    public String secure() {
        return "This is a secured API";
    }

    @GetMapping("/admin/dashboard")
    public String admin() {
        return "Admin Dashboard Access";
    }
    @GetMapping("/user/profile")
    public String user() {
        return "User Profile";
    }

    @GetMapping("/employee/data")
    public String employee() {
        return "Employee Data";
    }
}