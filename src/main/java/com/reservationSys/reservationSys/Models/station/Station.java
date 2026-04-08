package com.reservationSys.reservationSys.Models.station;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID stationId;

    private String name;

    private String city;

    private String town;

    private double latitude;

    private double longitude;

    private Instant createdAt;

}
