package com.reservationSys.reservationSys.Services.Car;

import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarRequestDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CarVerificationService {

    @Async
    public void verifyCar(UUID carId) {



    }
}
