package com.reservationSys.reservationSys.Services.Car;

import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CarUpdateService {
    private final CarRepo carRepo;

    public CarUpdateService(CarRepo carRepo) {
        this.carRepo = carRepo;
    }

    @Transactional
    public void updateCarStatus(UUID carId, CarStatus status) {

        Car carToUpdate= carRepo.findById(carId).orElseThrow(()-> new RessourceNotFound("Car with id "+carId+" not found"));

        carToUpdate.setVerifiedAt(Instant.now());
        carToUpdate.setStatus(status);
        carRepo.save(carToUpdate);


    }

}
