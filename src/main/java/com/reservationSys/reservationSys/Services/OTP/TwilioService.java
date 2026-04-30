package com.reservationSys.reservationSys.Services.OTP;


import java.util.UUID;


@Deprecated
public class TwilioService {
    private final VonageWhatsappService vonageWhatsappService;

    public TwilioService(VonageWhatsappService vonageWhatsappService) {
        this.vonageWhatsappService = vonageWhatsappService;
    }

    public void sendSms(String toPhone, String message) {
        String formattedMessage = "This is your confirmation code from the E-Car Charging Rental System: " + message;
        vonageWhatsappService.sendWhatsappMessage(toPhone, formattedMessage);
    }

    public boolean verifyEmailCode(UUID userId, String code) {
        return vonageWhatsappService.verifyEmailCode(userId, code);
    }
}
