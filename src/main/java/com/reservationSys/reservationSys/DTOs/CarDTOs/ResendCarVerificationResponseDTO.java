package com.reservationSys.reservationSys.DTOs.CarDTOs;

import lombok.Data;

@Data
public class ResendCarVerificationResponseDTO {

    String message;
    String plateNumber;

    public ResendCarVerificationResponseDTO(String s, String plateNumber) {
        this.message = s;
        this.plateNumber = plateNumber;
    }
}
