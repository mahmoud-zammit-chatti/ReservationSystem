package com.reservationSys.reservationSys.DTOs;


import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneVerificationDTO {

    @Pattern(regexp = "^[0-9]{6}", message = "The code must be exactly 6 digits")
    private String code;

}
