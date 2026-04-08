package com.reservationSys.reservationSys.DTOs.StationDTOs;

import com.reservationSys.reservationSys.Models.reservation.Duration;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;


@Data
@Getter
@Builder
public class StationNearMeRequestDTO {
    @NotNull(message = "Latitude is required")
    private double latitude;
    @NotNull(message = "Longitude is required")
    private double longitude;
    @NotNull(message = "Radius is required")
    private double radius;
    @FutureOrPresent(message = "Start time must be in the present or future")
    private Instant startTime;
    @NotNull
    private Duration duration;
    @NotNull
    private boolean availableOnly;
}
