package com.reservationSys.reservationSys.DTOs.CarDTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AddCarResponseDTO {

    private String PlateNumber;
    private String ChassisNumber;
    private Instant RegisteredAt;

}
