package com.reservationSys.reservationSys.DTOs.StationDTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StationResponseDTO {

    private String stationName;
    private String cityName;
    private String townName;
    private Instant createdAt;

}
