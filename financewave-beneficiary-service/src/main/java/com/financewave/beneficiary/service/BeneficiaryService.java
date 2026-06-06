package com.financewave.beneficiary.service;

import com.financewave.beneficiary.dto.*;
import com.financewave.beneficiary.entity.Beneficiary;
import com.financewave.beneficiary.repository.BeneficiaryRepository;
import com.financewave.beneficiary.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeneficiaryService {

    @Autowired
    private BeneficiaryRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================
    // ADD
    // =========================
    public ApiResponse<BeneficiaryResponse> add(String token, AddBeneficiaryRequest req) {

        String username = jwtUtil.extractUsername(token).toLowerCase();

        if (req.getAccountNumber() == null || req.getAccountNumber().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }

        if (req.getIfsc() == null || req.getIfsc().isEmpty()) {
            throw new RuntimeException("IFSC is required");
        }

        Beneficiary b = new Beneficiary();
        b.setUsername(username);
        b.setBeneficiaryAccountNumber(req.getAccountNumber());
        b.setNickname(req.getNickname());
        b.setBankName(req.getBankName());
        b.setIfscCode(req.getIfsc());
        b.setStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());

        repo.save(b);

        return new ApiResponse<>("SUCCESS", "Beneficiary added. Awaiting approval", map(b));
    }

    // =========================
    // MY LIST
    // =========================
    public ApiResponse<List<BeneficiaryResponse>> myList(String token) {

        String username = jwtUtil.extractUsername(token).toLowerCase();

        List<BeneficiaryResponse> list = repo
                .findByUsernameIgnoreCase(username)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "Fetched", list);
    }

    // =========================
    // DELETE
    // =========================
    public ApiResponse<String> delete(String token, Long id) {

        String username = jwtUtil.extractUsername(token).toLowerCase();

        Beneficiary b = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

        if (!b.getUsername().equalsIgnoreCase(username)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(b.getStatus())) {
            throw new RuntimeException("Only pending beneficiary can be deleted");
        }

        repo.delete(b);

        return new ApiResponse<>("SUCCESS", "Deleted successfully", null);
    }

    // =========================
    // ADMIN: PENDING
    // =========================
    public ApiResponse<List<BeneficiaryResponse>> getPending(String token) {

        checkAdmin(token);

        List<BeneficiaryResponse> list = repo.findByStatus("PENDING")
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "Pending list", list);
    }

    // =========================
    // ADMIN: APPROVE
    // =========================
    public ApiResponse<String> approve(String token, Long id) {

        checkAdmin(token);

        Beneficiary b = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

        if (!"PENDING".equals(b.getStatus())) {
            throw new RuntimeException("Only pending can be approved");
        }

        b.setStatus("ACTIVE");
        repo.save(b);

        return new ApiResponse<>("SUCCESS", "Approved successfully", null);
    }

    // =========================
    // ADMIN: REJECT
    // =========================
    public ApiResponse<String> reject(String token, Long id, String reason) {

        checkAdmin(token);

        Beneficiary b = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

        if (!"PENDING".equals(b.getStatus())) {
            throw new RuntimeException("Only pending can be rejected");
        }

        b.setStatus("REJECTED");
        repo.save(b);

        return new ApiResponse<>("SUCCESS", "Rejected: " + reason, null);
    }

    // =========================
    // ADMIN: ALL
    // =========================
    public ApiResponse<List<BeneficiaryResponse>> getAll(String token) {

        checkAdmin(token);

        List<BeneficiaryResponse> list = repo.findAll()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return new ApiResponse<>("SUCCESS", "All beneficiaries", list);
    }

    // =========================
    // VALIDATE (IMPORTANT FOR TRANSACTION)
    // =========================
    public boolean validate(String token, String account) {

        String username = jwtUtil.extractUsername(token).toLowerCase();

        System.out.println("VALIDATE USER: " + username);
        System.out.println("VALIDATE ACCOUNT: " + account);

        return repo.existsByUsernameIgnoreCaseAndBeneficiaryAccountNumberAndStatus(
                username,
                account,
                "ACTIVE"
        );
    }
    // =========================
    // ROLE CHECK
    // =========================
    private void checkAdmin(String token) {

        String role = jwtUtil.extractRole(token);

        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("Access denied. Admin only");
        }
    }

    // =========================
    // MAPPER
    // =========================
    private BeneficiaryResponse map(Beneficiary b) {
        return new BeneficiaryResponse(
                b.getId(),
                b.getBeneficiaryAccountNumber(),
                b.getNickname(),
                b.getBankName(),
                b.getStatus()
        );
    }
}