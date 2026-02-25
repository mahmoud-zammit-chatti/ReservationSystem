package com.reservationSys.reservationSys.Domain.port;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Port{
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    private UUID stationId;

    private String accessIdentifier;

    private PortStatus status=PortStatus.AVAILABLE;
}
