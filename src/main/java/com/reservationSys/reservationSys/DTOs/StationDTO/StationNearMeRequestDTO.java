package com.reservationSys.reservationSys.DTOs.StationDTO;

import com.reservationSys.reservationSys.Domain.reservation.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;


@Data
@Getter
@Builder
public class StationNearMeRequestDTO {
    private double latitude;
    private double longitude;
    private double radius;
    private Instant startTime;
    private Duration duration;
    private boolean availableOnly;
}
