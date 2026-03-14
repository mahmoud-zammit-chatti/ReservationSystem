package com.reservationSys.reservationSys.Services.Car;


import com.reservationSys.reservationSys.Cloud.AzureBlobStorageService;
import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarRequestDTO;
import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarResponseDTO;
import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicateChassisNumberException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicatePlateNumberException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepo carRepo;
    private final CarVerificationService carVerificationService;
    private final AzureBlobStorageService azureBlobStorageService;

    public CarService(CarRepo carRepo, CarVerificationService carVerificationService, AzureBlobStorageService azureBlobStorageService) {
        this.carRepo = carRepo;
        this.carVerificationService = carVerificationService;
        this.azureBlobStorageService = azureBlobStorageService;
    }


    public AddCarResponseDTO addCar(AppUser authUser,AddCarRequestDTO car) {

        Optional<Car> firstCheck = carRepo.findByPlateNumber(car.getPlateNumber());
        if(firstCheck.isPresent()) {
            throw new DuplicatePlateNumberException("A car with the same plate number already exists.");
        }
        Optional<Car>secondCheck =carRepo.findByChassisNumber(car.getChassisNumber());
        if(secondCheck.isPresent()) {
            throw new DuplicateChassisNumberException("A car with the same chassis number already exists.");
        }

        //saves image to azure blob and get the url

        String carteGriseImageUrl =  azureBlobStorageService.uploadImage(car.getCarteGriseImage(),authUser.getId());

        Car carToAdd = Car.builder()
                .plateNumber(car.getPlateNumber())
                .chassisNumber(car.getChassisNumber())
                .status(CarStatus.UNVERIFIED)
                .registeredAt(Instant.now())
                .verifiedAt(null)
                .userId(authUser.getId())
                .carteGriseUrl(carteGriseImageUrl)
                .build();

            carRepo.save(carToAdd);

            // Asynchronously verify the car after saving it to the database
        carVerificationService.verifyCar(carToAdd.getId());


    return AddCarResponseDTO.builder()
            .ChassisNumber(car.getChassisNumber())
            .PlateNumber(carToAdd.getPlateNumber())
            .RegisteredAt(carToAdd.getRegisteredAt())
            .build();
    }
}
