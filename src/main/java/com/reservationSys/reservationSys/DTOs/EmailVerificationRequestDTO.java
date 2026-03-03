package com.reservationSys.reservationSys.DTOs;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailVerificationRequestDTO {
    @Email
    private String email;
}
