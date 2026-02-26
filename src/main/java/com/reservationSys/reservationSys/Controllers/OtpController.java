package com.reservationSys.reservationSys.Controllers;


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

    @Value("${TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    @Value("${TWILIO_PHONE_NUMBER}")
    private String twilioPhoneNumber;

    @GetMapping("/sms")
    public ResponseEntity<String> getOtp(){
        Twilio.init(twilioAccountSid, twilioAuthToken);

        Message.creator(
                new PhoneNumber("+21640646012"),
                new PhoneNumber(twilioPhoneNumber),
                "hello 7awel 7awel"
                )
                .create();
        return ResponseEntity.ok("OTP sent successfully");

    }

}
