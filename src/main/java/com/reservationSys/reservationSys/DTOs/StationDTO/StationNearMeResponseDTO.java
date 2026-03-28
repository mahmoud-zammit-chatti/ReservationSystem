package com.reservationSys.reservationSys.DTOs.StationDTO;

import com.reservationSys.reservationSys.Domain.port.Port;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StationNearMeResponseDTO {

    UUID stationId;
    String stationName;
    double distance;
    double latitude;
    double longitude;
    List<Port> allPorts;
    List<Port> availablePortsForSLot;

}
