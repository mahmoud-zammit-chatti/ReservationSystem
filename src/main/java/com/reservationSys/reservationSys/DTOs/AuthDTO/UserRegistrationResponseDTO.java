package com.reservationSys.reservationSys.DTOs.AuthDTO;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserRegistrationResponseDTO {
    private String fullName;
    private String phoneNumber;
    private String email;

    private String SmsMsg;
    private boolean SmsSent;

    private String emailMsg;
    private boolean emailSent;
}
