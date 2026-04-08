package com.reservationSys.reservationSys.Controllers;

import com.reservationSys.reservationSys.DTOs.CarDTOs.*;
import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Services.Car.CarService;
import com.reservationSys.reservationSys.Services.Car.CarUpdateService;
import com.reservationSys.reservationSys.Services.Car.CarVerificationService;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/car")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;

    }


    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<AddCarResponseDTO> addCar(
            @AuthenticationPrincipal MyAppUserDetails userDetails,
           @Valid @ModelAttribute AddCarRequestDTO car
    ) {
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.addCar(currentUser,car));
    }


    @PutMapping(value="/{carId}/resend-verification",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<ResendCarVerificationResponseDTO>resendVerification(
           @Valid @ModelAttribute CarVerificationRequestDTO request,
            @AuthenticationPrincipal MyAppUserDetails userDetails,
            @PathVariable UUID carId){

        AppUser currentUser = userDetails.getAppUser();

    return ResponseEntity.accepted().body(carService.resendVerification(currentUser,carId,request.getCarteGrise()));
    }

    @GetMapping("/my-cars")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<ArrayList<CarResponseDTO>> getMyCars(@AuthenticationPrincipal MyAppUserDetails userDetails){
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.getCarsForUser(currentUser));
    }

    @GetMapping("/{carId}/my-car")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<CarResponseDTO> getMyCar(@AuthenticationPrincipal MyAppUserDetails userDetails, @PathVariable UUID carId){
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.getCarByIdForUser(currentUser,carId));
    }

    @DeleteMapping("/delete-all-cars")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<List<CarResponseDTO>> deleteAllCarsForUser(@AuthenticationPrincipal MyAppUserDetails userDetails){
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.deleteAllCarsForUser(currentUser));
    }

    @DeleteMapping("/{carId}/delete")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<CarResponseDTO> deleteCarByIdForUser(@AuthenticationPrincipal MyAppUserDetails userDetails, @PathVariable UUID carId){
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.deleteCarByIdForUser(currentUser,carId));
     }


}
