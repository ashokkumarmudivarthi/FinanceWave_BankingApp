package com.financewave.transaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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
    // DEPOSIT (STRICT VALIDATION)
    // =========================
    public void deposit(String accNo, double amount, String token) {

        try {
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

        } catch (HttpStatusCodeException ex) {
            // If Account Service throws error
            throw new RuntimeException("Deposit failed: " + ex.getResponseBodyAsString());

        } catch (Exception e) {
            throw new RuntimeException("Deposit failed: " + e.getMessage());
        }
    }

    // =========================
    // WITHDRAW (STRICT + SAFE)
    // =========================
    public void withdraw(String accNo, double amount, String token) {

        try {
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

        } catch (HttpStatusCodeException ex) {
            // 🔥 Most important: propagate real error
            throw new RuntimeException("Withdraw failed: " + ex.getResponseBodyAsString());

        } catch (Exception e) {
            throw new RuntimeException("Withdraw failed: " + e.getMessage());
        }
    }
}