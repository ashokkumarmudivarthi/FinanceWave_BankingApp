package com.financewave.account.service;

import com.financewave.account.entity.VpaMapping;
import com.financewave.account.repository.VpaRepository;
import com.financewave.account.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpiService {

    @Autowired
    private VpaRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // CREATE VPA
    // =========================
    public String create(String token, String vpa, String accountNumber) {

        String username = jwtUtil.extractUsername(token);

        if (repo.existsByVpa(vpa)) {
            throw new RuntimeException("VPA already exists");
        }

        VpaMapping v = new VpaMapping();
        v.setVpa(vpa);
        v.setAccountNumber(accountNumber);
        v.setUsername(username);

        repo.save(v);

        return "VPA created successfully";
    }

    // =========================
    // GET ACCOUNT FROM VPA
    // =========================
    public String getAccount(String vpa) {

        VpaMapping v = repo.findByVpa(vpa)
                .orElseThrow(() -> new RuntimeException("VPA not found"));

        return v.getAccountNumber();
    }

    // =========================
    // VALIDATE VPA
    // =========================
    public boolean validate(String vpa) {
        return repo.existsByVpa(vpa);
    }
}