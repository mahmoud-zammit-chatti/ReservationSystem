package com.reservationSys.reservationSys.Services.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final String baseUrl;

    public EmailService(
        @Value("${brevo.api-key}") String apiKey,
        @Value("${brevo.sender.email}") String senderEmail,
        @Value("${brevo.sender.name:VoltBook}") String senderName,
        @Value("${brevo.base-url:https://api.brevo.com/v3}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.baseUrl = baseUrl;
    }

    public void sendVerificationEmail(
        String to,
        String verificationCode,
        String subject
    ) {
        Map<String, Object> body = Map.of(
            "sender", Map.of("name", senderName, "email", senderEmail),
            "to", List.of(Map.of("email", to)),
            "subject", subject,
            "textContent", "this is your confirmation code : " + verificationCode
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(baseUrl + "/smtp/email", request, String.class);
        } catch (RestClientException e) {
            throw new MailSendException(
                "Brevo email failed for recipient: " + to,
                e
            );
        }
    }
}
