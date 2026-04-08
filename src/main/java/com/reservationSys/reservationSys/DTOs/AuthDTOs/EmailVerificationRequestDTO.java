package com.reservationSys.reservationSys.DTOs.AuthDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationRequestDTO {
    @Email
    @NotBlank
    private String email;
}
