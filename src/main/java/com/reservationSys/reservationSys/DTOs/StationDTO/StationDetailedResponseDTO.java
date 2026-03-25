package com.reservationSys.reservationSys.DTOs.StationDTO;

import com.reservationSys.reservationSys.Domain.port.Port;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StationDetailedResponseDTO {

    private StationResponseDTO station;
    private List<Port> ports;

}
