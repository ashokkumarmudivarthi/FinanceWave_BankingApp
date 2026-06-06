package com.financewave.transaction.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BeneficiaryClient {

    @Value("${beneficiary.service.url}")
    private String baseUrl;

    private RestTemplate rest = new RestTemplate();

    public boolean validate(String token, String accNo) {

        try {
            String url = baseUrl + "/beneficiaries/validate?account=" + accNo;

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
            return false;
        }
    }
}