package com.reservationSys.reservationSys.Controllers;



import com.reservationSys.reservationSys.Services.auth.EmailService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;


@RestController
@RequestMapping("api/v1/otp")
public class OtpController {

    private final EmailService emailService;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    public OtpController(EmailService emailService) {
        this.emailService = emailService;
    }


}
