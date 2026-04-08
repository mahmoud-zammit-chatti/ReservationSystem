package com.reservationSys.reservationSys.DTOs.AuthDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUserDTO {

    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;

}
