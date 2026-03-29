package com.reservationSys.reservationSys.DTOs.AuthDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class EmailVerificationDTO {

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be a 6-digit number")
    private String code;
}
