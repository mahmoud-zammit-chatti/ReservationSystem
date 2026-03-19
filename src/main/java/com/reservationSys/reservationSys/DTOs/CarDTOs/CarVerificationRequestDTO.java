package com.reservationSys.reservationSys.DTOs.CarDTOs;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CarVerificationRequestDTO {

    private MultipartFile carteGrise;
}
