package com.reservationSys.reservationSys.DTOs.ReservationDTOs;


import com.reservationSys.reservationSys.Domain.reservation.Duration;
import com.reservationSys.reservationSys.Domain.reservation.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReservationResponseDTO {

    private UUID reservationId;
    private UUID userId;
    private UUID carId;
    private UUID portId;
    private Duration duration;
    private String contactNumber;
    private int startTime;
    private int endTime;
    private LocalDate startDate;
    private ReservationStatus status;


}
