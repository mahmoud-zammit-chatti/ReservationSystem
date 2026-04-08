package com.reservationSys.reservationSys.DTOs.CarDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CarVerificationRequestDTO {

    @NotNull(message = "Carte Grise is required")
    private MultipartFile carteGrise;
}
