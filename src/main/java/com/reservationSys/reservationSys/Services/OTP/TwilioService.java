package com.reservationSys.reservationSys.Services.OTP;


import com.reservationSys.reservationSys.exceptions.SmsDeliveryException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class TwilioService {
    @Value("${TWILIO_ACCOUNT_SID}")
    private String twilioAccountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    @Value("${TWILIO_PHONE_NUMBER}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init(){
        Twilio.init(twilioAccountSid, twilioAuthToken);

    }

    public void sendSms(String toPhone,String message) {

        try {
            Message.creator(
                            new PhoneNumber(toPhone),
                            new PhoneNumber(twilioPhoneNumber),
                            "This is your confirmation code from the E-Car Charging Rental System: " + message
                    )
                    .create();

        }catch(Exception e){
            throw new SmsDeliveryException("Failed to send SMS: " + e.getMessage());
        }
    }


}
