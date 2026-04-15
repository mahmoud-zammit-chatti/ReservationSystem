package com.reservationSys.reservationSys.Services.Car;

import com.reservationSys.reservationSys.Models.car.Car;
import com.reservationSys.reservationSys.Models.car.CarStatus;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.Exceptions.CarExceptions.BlockedCarException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class CarUpdateService {
    private final CarRepo carRepo;

    public CarUpdateService(CarRepo carRepo) {
        this.carRepo = carRepo;
    }

    @Transactional
    public void updateCarStatus(UUID carId, boolean isVerified ) {
        Car carToUpdate = carRepo.findById(carId).orElseThrow(() -> new ResourceNotFound("Car with id " + carId + " not found"));

        if(isVerified) {
            carToUpdate.setStatus(CarStatus.VERIFIED);
            carToUpdate.setVerifiedAt(Instant.now());
        }else{
            carToUpdate.setVerificationAttempts(carToUpdate.getVerificationAttempts() + 1);
            if(carToUpdate.getVerificationAttempts()>=5) {
                carToUpdate.setStatus(CarStatus.BLOCKED);
                carToUpdate.setBlockedAt(Instant.now());
            }
        }


            carRepo.save(carToUpdate);
    }

        public void tryUnblock(UUID carId) {
            Car carToUnblock = carRepo.findById(carId).orElseThrow(() -> new ResourceNotFound("Car with id " + carId + " not found"));
            if(carToUnblock.getBlockedAt().isBefore(Instant.now().minus(24, ChronoUnit.HOURS))) {
                carToUnblock.setBlockedAt(null);
                carToUnblock.setStatus(CarStatus.UNVERIFIED);
                carToUnblock.setVerificationAttempts(0);
            carRepo.save(carToUnblock);
            }else{
                long totalMinutesRemaining = (24 * 60) - ChronoUnit.MINUTES.between(carToUnblock.getBlockedAt(), Instant.now());
                long hoursRemaining = totalMinutesRemaining / 60;
                long minutesRemaining = totalMinutesRemaining % 60;
                throw new BlockedCarException("Car is still blocked.", hoursRemaining, minutesRemaining);            }

        }



}
