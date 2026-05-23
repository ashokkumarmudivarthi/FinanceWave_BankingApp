package com.financewave.transaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.financewave.transaction.dto.ApiResponse;

@Component
public class AccountClient {

    @Value("${account.service.url}")
    private String baseUrl;

    private RestTemplate rest = new RestTemplate();

    // =========================
    // VALIDATE ACCOUNT
    // =========================
    public boolean validateAccount(String accNo, String token) {

        try {
            String url = baseUrl + "/accounts/validate/" + accNo;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = rest.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );

            return response.getBody() != null && response.getBody();

        } catch (Exception e) {
            System.out.println("ERROR calling account service: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // DEPOSIT
    // =========================
    /*
     * public boolean deposit(String accNo, double amount, String token) {

        try {
            String url = baseUrl + "/accounts/deposit/" + accNo + "?amount=" + amount;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = rest.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.out.println("Deposit failed: " + e.getMessage());
            return false;
        }
    }*/
    
    public void deposit(String accNo, double amount, String token) {

        String url = baseUrl + "/accounts/deposit/" + accNo + "?amount=" + amount;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> response = rest.exchange(
                url,
                HttpMethod.PUT,
                entity,
                ApiResponse.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from account service");
        }

        if (!"SUCCESS".equals(response.getBody().getStatus())) {
            throw new RuntimeException(response.getBody().getMessage());
        }
    }

    // =========================
    // WITHDRAW
    // =========================
    /* i can able to withdraw 500000 rupees even i have 5000 rupees
     * public boolean withdraw(String accNo, double amount, String token) {

        try {
            String url = baseUrl + "/accounts/withdraw/" + accNo + "?amount=" + amount;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = rest.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.out.println("Withdraw failed: " + e.getMessage());
            return false;
        }
    }*/
    public void withdraw(String accNo, double amount, String token) {

        String url = baseUrl + "/accounts/withdraw/" + accNo + "?amount=" + amount;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> response = rest.exchange(
                url,
                HttpMethod.PUT,
                entity,
                ApiResponse.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from account service");
        }

        if (!"SUCCESS".equals(response.getBody().getStatus())) {
            throw new RuntimeException(response.getBody().getMessage());
        }
    }
}