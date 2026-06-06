package com.financewave.transaction.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UpiClient {

    @Value("${account.service.url}")
    private String baseUrl;

    private RestTemplate rest = new RestTemplate();

    // ✅ VALIDATE VPA WITH TOKEN
    public boolean validateVpa(String vpa, String token) {

        String url = baseUrl + "/upi/validate?vpa=" + vpa;

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
    }

    // ✅ GET ACCOUNT FROM VPA WITH TOKEN
    public String getAccountFromVpa(String vpa, String token) {

        String url = baseUrl + "/upi/account?vpa=" + vpa;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = rest.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }
}