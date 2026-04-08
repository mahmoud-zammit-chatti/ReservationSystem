package com.reservationSys.reservationSys.Services.Port;


import com.reservationSys.reservationSys.Domain.reservation.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PortAvailabilityCheckService {
    @Transactional
    protected boolean isTimeSLotAvailable(Instant startTime, Instant endTime , List<Reservation> reservationList){
        for(Reservation res : reservationList){
            if(
                    res.getStartTime().isBefore(endTime) &&
                            res.getEndTime().isAfter(startTime)
            )return false;
        }
        return true;
    }

}
