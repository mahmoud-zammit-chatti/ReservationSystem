package com.reservationSys.reservationSys.DTOs.StationDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationRequestDTO {

    @NotBlank(message = "Station name is required")
    private String stationName;
    @NotNull(message = "Longitude is required")
    private double longitude;
    @NotNull(message = "Latitude is required")
    private double latitude;
    @NotBlank(message = "City is required")
    private String city;
    
    private String town;


}
