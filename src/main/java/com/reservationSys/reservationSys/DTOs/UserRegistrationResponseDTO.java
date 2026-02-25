package com.reservationSys.reservationSys.DTOs;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserRegistrationResponseDTO {
    private String fullName;
    private String phoneNumber;
    private String email;
}
