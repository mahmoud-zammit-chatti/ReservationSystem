package com.reservationSys.reservationSys.DTOs.PortDTOs;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PortAddRequestDTO {
    String portName;
}
