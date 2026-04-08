package com.reservationSys.reservationSys.DTOs.PortDTOs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PortAddRequestDTO {

    @NotBlank(message = "Port name is required")
    String portName;
}
