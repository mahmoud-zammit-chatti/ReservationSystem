package com.reservationSys.reservationSys.DTOs.AuthDTOs;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailVerificationRequestDTO {
    @Email
    private String email;
}
