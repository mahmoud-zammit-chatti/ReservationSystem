package com.reservationSys.reservationSys.DTOs;


import lombok.Data;

@Data
public class RegisterUserDTO {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
}
