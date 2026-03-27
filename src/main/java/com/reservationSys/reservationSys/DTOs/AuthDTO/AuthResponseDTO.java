package com.reservationSys.reservationSys.DTOs.AuthDTO;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;

}
