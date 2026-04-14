package com.reservationSys.reservationSys.DTOs.ReservationDTOs;


import com.reservationSys.reservationSys.Models.reservation.Duration;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationAddRequestDTO {

    @NotNull(message = "Port ID is required")
    private UUID portId;
    @NotNull(message = "Car ID is required")
    private UUID carId;

    @NotNull(message = "Duration is required")
    private Duration duration;
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "Contact number must be exactly 8 digits")
    private String contactNumber;
    @NotNull(message = "Start time hour is required")
    @Min(value = 0,message = "Start time hour must be between 0 and 23")
    @Max(value = 23,message = "Start time hour must be between 0 and 23")
    private int startTimeHour;
    @NotNull
    @FutureOrPresent(message = "Start date must be today or in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;


}
