package com.reservationSys.reservationSys.Services.Port;


import com.reservationSys.reservationSys.Models.port.Port;
import com.reservationSys.reservationSys.Models.port.PortStatus;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.RessourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class PortStatusUpdateService {

    private final PortRepo portRepo;

    public PortStatusUpdateService(PortRepo portRepo) {
        this.portRepo = portRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePortStatus(UUID portId, UUID stationId){
        Port port = portRepo.findByIdAndStation_StationId(portId,stationId).orElseThrow(()-> new RessourceNotFound("can't find the requested port"));

        port.setStatus(PortStatus.EXPIRING_SOON);
        portRepo.save(port);
    }
}

