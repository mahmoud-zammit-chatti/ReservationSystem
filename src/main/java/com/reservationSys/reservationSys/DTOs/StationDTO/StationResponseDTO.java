package com.reservationSys.reservationSys.DTOs.StationDTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StationResponseDTO {

    private UUID stationId;
    private String stationName;
    private double latitude;
    private double longitude;
    private String cityName;
    private String townName;
    private Instant createdAt;

}
