package com.reservationSys.reservationSys.Domain.car;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    private CarStatus status;

    private Instant registeredAt;

    private Instant verifiedAt;
}
