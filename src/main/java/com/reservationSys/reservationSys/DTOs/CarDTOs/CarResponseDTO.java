package com.reservationSys.reservationSys.DTOs.CarDTOs;

import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class CarResponseDTO {
    private UUID carId;
    private String plateNumber;
    private CarStatus status;
    private Instant registrationDate;


}
