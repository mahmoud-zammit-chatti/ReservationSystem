package com.reservationSys.reservationSys.DTOs.CarDTOs;

import com.reservationSys.reservationSys.Models.car.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Data
public class CarResponseDTO {
    private UUID carId;
    private String plateNumber;
    private CarStatus status;
    private Instant registrationDate;


}
