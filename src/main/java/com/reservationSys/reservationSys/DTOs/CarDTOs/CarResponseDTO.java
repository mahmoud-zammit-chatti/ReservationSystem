package com.reservationSys.reservationSys.DTOs.CarDTOs;

import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Data
public class CarResponseDTO {

    private String plateNumber;
    private CarStatus status;
    private Instant registrationDate;


}
