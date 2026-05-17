package com.financewave.transaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountClient {

    @Value("${account.service.url}")
    private String baseUrl;

    public boolean validateAccount(String accNo, String token) {

        try {
            RestTemplate rest = new RestTemplate();

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
}