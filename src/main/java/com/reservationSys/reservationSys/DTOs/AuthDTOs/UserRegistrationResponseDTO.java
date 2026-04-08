package com.reservationSys.reservationSys.DTOs.AuthDTOs;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserRegistrationResponseDTO {
    private String fullName;
    private String phoneNumber;
    private String email;

    private String smsMsg;
    private boolean smsSent;

    private String emailMsg;
    private boolean emailSent;
}
