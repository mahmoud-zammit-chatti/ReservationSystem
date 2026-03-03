package com.reservationSys.reservationSys.DTOs;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneVerificationRequestDTO {


        @Pattern(regexp = "^[0-9]{8}", message = "The phone number must be exactly 8 digits")
        String phoneNumber;

}
