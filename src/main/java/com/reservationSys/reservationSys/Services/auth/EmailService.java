package com.reservationSys.reservationSys.Services.auth;


import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    private final String from;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from}") String from
    ) {
        this.resend = new Resend(apiKey);
        this.from = from;
    }

    public void sendVerificationEmail(String to, String verificationCode, String subject) {
        CreateEmailOptions message = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .text("this is your confirmation code : " + verificationCode)
                .build();

        try {
            resend.emails().send(message);
        } catch (ResendException e) {
            throw new MailSendException("Resend email failed for recipient: " + to, e);
        }
    }
}
