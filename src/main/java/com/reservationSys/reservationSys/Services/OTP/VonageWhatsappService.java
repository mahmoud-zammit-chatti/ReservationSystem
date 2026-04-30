package com.reservationSys.reservationSys.Services.OTP;

import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import com.reservationSys.reservationSys.Models.otp.OTP;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.otp.OtpStatus;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VonageWhatsappService {
    @Value("${vonage.api-key}")
    private String vonageApiKey;

    @Value("${vonage.api-secret}")
    private String vonageApiSecret;

    @Value("${vonage.whatsapp.from}")
    private String whatsappFrom;

    @Value("${vonage.messages.base-url:https://messages-sandbox.nexmo.com/v1/messages}")
    private String messagesBaseUrl;

    private final OtpRepo otpRepo;
    private final AppUserRepo appUserRepo;
    private final RestTemplate restTemplate;

    public VonageWhatsappService(OtpRepo otpRepo, AppUserRepo appUserRepo, RestTemplateBuilder restTemplateBuilder) {
        this.otpRepo = otpRepo;
        this.appUserRepo = appUserRepo;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendWhatsappMessage(String toPhone, String message) {
        String normalizedTo = normalizeWhatsappNumber(toPhone);
        String normalizedFrom = normalizeWhatsappNumber(whatsappFrom);

        Map<String, Object> payload = Map.of(
                "from", normalizedFrom,
                "to", normalizedTo,
                "message_type", "text",
                "text", message,
                "channel", "whatsapp"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(vonageApiKey, vonageApiSecret);

        restTemplate.postForEntity(messagesBaseUrl, new HttpEntity<>(payload, headers), String.class);
    }

    public boolean verifyEmailCode(UUID userId, String code) {
        AppUser appUser = appUserRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User with id: " + userId + " not found"));

        OTP otp = otpRepo.findByUserIdAndPurposeAndStatus(userId, OtpPurpose.EMAIL_VERIFICATION, OtpStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFound("No pending OTP found for user with email: " + appUser.getEmail()));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepo.save(otp);
            throw new ResourceNotFound("Email OTP has expired, Please request for another verification email code");
        }

        if (otp.getCode().equals(code)) {
            otp.setStatus(OtpStatus.VERIFIED);
            otp.setVerifiedAt(Instant.now());
            otpRepo.save(otp);
            return true;
        }
        return false;
    }

    private String normalizeWhatsappNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;
    }
}

