package com.reservationSys.reservationSys.Services.OTP;


import com.reservationSys.reservationSys.Domain.otp.OTP;
import com.reservationSys.reservationSys.Domain.otp.OtpPurpose;
import com.reservationSys.reservationSys.Domain.otp.OtpStatus;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import com.reservationSys.reservationSys.exceptions.GeneralExceptions.RessourceNotFound;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
public class TwilioService {
    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    private final OtpRepo otpRepo;
        private final AppUserRepo appUserRepo;

    public TwilioService(OtpRepo otpRepo, AppUserRepo appUserRepo) {
        this.otpRepo = otpRepo;
        this.appUserRepo = appUserRepo;
    }

    @PostConstruct
    public void init(){
        Twilio.init(twilioAccountSid, twilioAuthToken);

    }

    public void sendSms(String toPhone,String message) {


            Message.creator(
                            new PhoneNumber(toPhone),
                            new PhoneNumber(twilioPhoneNumber),
                            "This is your confirmation code from the E-Car Charging Rental System: " + message
                    )
                    .create();




    }


    public boolean verifyEmailCode(UUID userId, String code) {
        AppUser appUser = appUserRepo.findById(userId).orElseThrow(() -> new RessourceNotFound("User with id: " + userId + " not found"));

        OTP otp = otpRepo.findByUserIdAndPurposeAndStatus(userId, OtpPurpose.EMAIL_VERIFICATION, OtpStatus.PENDING)
                .orElseThrow(() -> new RessourceNotFound("No pending OTP found for user with email: " + appUser.getEmail()));

        if(otp.getExpiresAt().isBefore(Instant.now())){
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepo.save(otp);
            throw new RessourceNotFound("Email OTP has expired, Please request for another verification email code");
        }

        if(otp.getCode().equals(code)){
            otp.setStatus(OtpStatus.VERIFIED);
            otp.setVerifiedAt(java.time.Instant.now());
            otpRepo.save(otp);
            return true;
        }else{
            return false;
        }
    }
}
