package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepo extends JpaRepository<Car, UUID> {

    Optional<Car> findById(UUID id);
    Optional<Car> findByIdAndUserId(UUID carId, UUID userId);

    Optional<Car> findByPlateNumberAndChassisNumber(String licensePlate, String chassisNumber);
    Optional<Car> findByPlateNumber(String plateNumber);
    Optional<Car> findByChassisNumber(String chassisNumber);

    Optional<Car> deleteByIdAndUserId(UUID carId, UUID id);
    @Query(
            " SELECT c FROM Car c WHERE c.blockedAt < :threshold AND c.status = :status"
    )
    List<Car> findAllByStatusAndBlockedAtBefore(CarStatus status, Instant threshold);


    List<Car> deleteAllByUserId(UUID userId);

    List<Car> findAllByUserId(UUID userId);

}
