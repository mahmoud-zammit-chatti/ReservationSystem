package com.reservationSys.reservationSys.Controllers;

import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarRequestDTO;
import com.reservationSys.reservationSys.DTOs.CarDTOs.AddCarResponseDTO;
import com.reservationSys.reservationSys.Domain.car.Car;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Services.Car.CarService;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/car")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }


    @PostMapping("/add")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<AddCarResponseDTO> addCar(@AuthenticationPrincipal MyAppUserDetails userDetails, @RequestBody AddCarRequestDTO car) {
        AppUser currentUser = userDetails.getAppUser();
        return ResponseEntity.ok(carService.addCar(currentUser,car));
    }



}
