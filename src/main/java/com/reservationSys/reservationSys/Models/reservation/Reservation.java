package com.reservationSys.reservationSys.Models.reservation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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

    private String contactNumber;

    private Instant startTime;

    @Enumerated
    private Duration duration;

    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Enumerated(EnumType.STRING)
    private CancellationReason cancellationReason;

    @Enumerated(EnumType.STRING)
    private PenaltyType penaltyType;

    private boolean penaltyWaived=false;

    private boolean lateCancel=false;

    private Instant createdAt;

    private Instant checkedInAt;

    private Instant cancelledAt;
}
