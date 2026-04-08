package com.reservationSys.reservationSys.Services.Car;


import com.reservationSys.reservationSys.Models.car.Car;
import com.reservationSys.reservationSys.Models.car.CarStatus;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CarSchedulerService {


    private final CarRepo carRepo;


    public CarSchedulerService(CarRepo carRepo) {
        this.carRepo = carRepo;

    }

    @Transactional
    @Scheduled(fixedDelayString = "${car_status_scheduler_interval}")
    public void unblockCar(){
        List<Car> list= carRepo.findAllByStatusAndBlockedAtBefore(CarStatus.BLOCKED, Instant.now().minus(24, ChronoUnit.HOURS));
        for  (Car car : list) {
            car.setStatus(CarStatus.UNVERIFIED);
            car.setVerificationAttempts(0);
            car.setBlockedAt(null);
        }
        carRepo.saveAll(list);
        System.out.println("unblocked cars by scheduler");
    }
}
