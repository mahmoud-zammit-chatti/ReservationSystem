package com.reservationSys.reservationSys.Models.car;

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
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private  String plateNumber;

    private String chassisNumber;

    private String carteGriseUrl;

    private int verificationAttempts=0;

    private Instant blockedAt;

    @Enumerated(EnumType.STRING)
    private CarStatus status;

    private Instant registeredAt;

    private Instant verifiedAt;
}
