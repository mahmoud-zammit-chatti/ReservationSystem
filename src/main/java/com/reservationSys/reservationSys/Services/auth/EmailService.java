package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.Domain.otp.OTP;
import com.reservationSys.reservationSys.Domain.otp.OtpPurpose;
import com.reservationSys.reservationSys.Domain.otp.OtpStatus;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailService {
    private final AppUserRepo appUserRepo;
    private final OtpRepo otpRepo;

    @Value("${spring.mail.username}")
    private String email;

    private final JavaMailSender mailSender;

    public EmailService(AppUserRepo appUserRepo, OtpRepo otpRepo, JavaMailSender mailSender) {
        this.appUserRepo = appUserRepo;
        this.otpRepo = otpRepo;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        AppUser appUser = appUserRepo.findByEmail(to).orElseThrow(() -> new RessourceNotFound("If this email exists, a verification code has been sent"));


        //creating an OTP object for the email sent for easier verification later; so its easier to retrieve the OTP object by the userId and having the sent code stored
        OTP verificationEmail = OTP.builder()
                .createdAt(Instant.now())
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .status(OtpStatus.PENDING)
                .code(verificationCode)
                .verifiedAt(null)
                .expiresAt(Instant.now().plusSeconds(60 * 60))
                .reservationId(null)
                .userId(appUser.getId())
                .build();


        //saving the otp object of the email sent for verification

        otpRepo.save(verificationEmail);

        message.setTo(to);
        message.setSubject("Email verification for E-Car reservation system");
        message.setText("this is your confirmation code : " + verificationCode);
        message.setFrom(email);

        mailSender.send(message);
    }



}

