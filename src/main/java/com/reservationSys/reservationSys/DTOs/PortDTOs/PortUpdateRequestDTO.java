package com.reservationSys.reservationSys.DTOs.PortDTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortUpdateRequestDTO {
    @NotBlank(message = "New port name is required")
    String newName;
}
