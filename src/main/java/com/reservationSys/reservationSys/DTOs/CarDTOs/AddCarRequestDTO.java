package com.reservationSys.reservationSys.DTOs.CarDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class AddCarRequestDTO {

    @Pattern(
        regexp = "^(\\d{2,3} تونس \\d{3,4}|\\d{5,6} نت)$",
        message = "Invalid plate number format. Accepted formats: NN تونس NNN, NNN تونس NNN, NN تونس NNNN, NNN تونس NNNN, NNNNN نت, NNNNNN نت"
    )
    @NotBlank
    private String plateNumber;

    @Pattern(
        regexp = "^[A-Z0-9]{17}$",
        message = "Invalid chassis number format. It must be exactly 17 characters long, containing only uppercase letters and digits."
    )
    @NotBlank
    private String chassisNumber;

    @NotNull
    private MultipartFile carteGriseImage;
}
