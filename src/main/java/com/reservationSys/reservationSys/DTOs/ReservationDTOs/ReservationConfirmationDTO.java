package com.reservationSys.reservationSys.DTOs.ReservationDTOs;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationConfirmationDTO {

    @Pattern(regexp = "\\d{6}", message = "Code must contain exactly 6 digits")
    private String code;


}
