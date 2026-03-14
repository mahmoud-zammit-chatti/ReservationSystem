package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.car.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CarRepo extends JpaRepository<Car, UUID> {

    Optional<Car> findById(UUID id);

    Optional<Car> findByPlateNumberAndChassisNumber(String licensePlate, String chassisNumber);
    Optional<Car> findByPlateNumber(String plateNumber);
    Optional<Car> findByChassisNumber(String chassisNumber);

    Optional<Car> findByUserId(UUID userId);
}
