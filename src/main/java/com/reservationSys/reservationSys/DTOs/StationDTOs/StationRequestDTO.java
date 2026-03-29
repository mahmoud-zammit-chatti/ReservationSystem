package com.reservationSys.reservationSys.DTOs.StationDTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StationRequestDTO {

    private String stationName;
    private double longitude;
    private double latitude;
    private String city;
    private String town;


}
