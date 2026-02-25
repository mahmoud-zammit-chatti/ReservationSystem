package com.reservationSys.reservationSys.Domain.car;

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
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private  String plateNumber;

    private String chassisNumber;

    private String tesla_uid;

    private CarStatus status=CarStatus.UNVERIFIED;

    private LocalDateTime registeredAt;

    private LocalDateTime verifiedAt;
}
