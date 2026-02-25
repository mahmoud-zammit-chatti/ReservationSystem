package com.reservationSys.reservationSys.Domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private UUID carId;
    private UUID portId;

    private LocalDateTime startTime;

    private Duration duration;

    private LocalDateTime endTime;

    private ReservationStatus reservationStatus=ReservationStatus.PENDING_OTP;

    private CancellationReason cancellationReason;

    private LocalDateTime createdAt;

    private LocalDateTime checkedInAt;
}
