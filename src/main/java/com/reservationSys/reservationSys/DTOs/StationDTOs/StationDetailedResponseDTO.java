package com.reservationSys.reservationSys.DTOs.StationDTOs;

import com.reservationSys.reservationSys.Models.port.Port;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StationDetailedResponseDTO {

    private StationResponseDTO station;
    private List<Port> ports;

}
