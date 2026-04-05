package com.reservationSys.reservationSys.DTOs.PortDTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortUpdateRequestDTO {
    String newName;
}
