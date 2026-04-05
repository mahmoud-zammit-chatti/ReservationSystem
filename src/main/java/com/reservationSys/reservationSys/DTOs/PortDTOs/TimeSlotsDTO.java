package com.reservationSys.reservationSys.DTOs.PortDTOs;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TimeSlotsDTO {
    private UUID portId;
    private String portName;
    private int startTime;
    private int endTime;

}
