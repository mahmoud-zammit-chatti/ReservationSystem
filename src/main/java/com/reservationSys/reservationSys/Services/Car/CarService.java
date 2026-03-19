package com.reservationSys.reservationSys.Services.Car;


import com.reservationSys.reservationSys.Cloud.AzureBlobStorageService;
import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarRequestDTO;
import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarResponseDTO;
import com.reservationSys.reservationSys.DTOs.CarDTOs.ResendCarVerificationResponseDTO;
import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.car.CarStatus;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.exceptions.CarExceptions.CarAlreadyVerifiedException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicateChassisNumberException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicatePlateNumberException;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarService {

    private final CarRepo carRepo;
    private final AzureBlobStorageService azureBlobStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final CarUpdateService carUpdateService;


    public CarService(CarRepo carRepo, AzureBlobStorageService azureBlobStorageService, ApplicationEventPublisher eventPublisher, CarUpdateService carUpdateService) {
        this.carRepo = carRepo;
        this.azureBlobStorageService = azureBlobStorageService;
        this.eventPublisher = eventPublisher;
        this.carUpdateService = carUpdateService;
    }


    @Transactional
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

        eventPublisher.publishEvent(new CarCreatedEvent(carToAdd.getId()));



        return AddCarResponseDTO.builder()
            .ChassisNumber(car.getChassisNumber())
            .PlateNumber(carToAdd.getPlateNumber())
            .RegisteredAt(carToAdd.getRegisteredAt())
            .build();
    }


    @Transactional
    public ResendCarVerificationResponseDTO resendVerification(AppUser authUser, UUID carId, MultipartFile carteGrise) {

        Car car = carRepo.findByIdAndUserId(carId, authUser.getId())
                .orElseThrow(() -> new RessourceNotFound("Car not found for the given ID and user."));

        if(car.getStatus()==CarStatus.VERIFIED) {
            throw new CarAlreadyVerifiedException("Car is already verified.");

        }
        if(car.getStatus()==CarStatus.BLOCKED){
            carUpdateService.tryUnblock(carId);
            System.out.println("car unblocked by  try unblock");


        }else{
            String url=azureBlobStorageService.uploadImage(carteGrise,authUser.getId());
            car.setCarteGriseUrl(url);

            carRepo.save(car);



        }
            eventPublisher.publishEvent(new CarCreatedEvent(car.getId()));
        return new ResendCarVerificationResponseDTO("a verification request was sent !("+car.getVerificationAttempts()+")",car.getPlateNumber());

    }



}
