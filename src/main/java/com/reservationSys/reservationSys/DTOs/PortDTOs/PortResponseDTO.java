package com.reservationSys.reservationSys.DTOs.PortDTOs;

import com.reservationSys.reservationSys.Domain.port.PortStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PortResponseDTO {
    UUID portId;
    UUID stationId;
    String portName;
    PortStatus portStatus;
    String accessIdentifier;
}
