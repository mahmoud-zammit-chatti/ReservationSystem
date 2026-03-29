package com.reservationSys.reservationSys.Services.Port;


import com.reservationSys.reservationSys.DTOs.PortDTOs.PortAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.Domain.port.Port;
import com.reservationSys.reservationSys.Domain.port.PortStatus;
import com.reservationSys.reservationSys.Domain.station.Station;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.StationRepo;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PortService {

    private final StationRepo stationRepo;
    private final PortRepo portRepo;

    public PortService(StationRepo stationRepo, PortRepo portRepo) {
        this.stationRepo = stationRepo;
        this.portRepo = portRepo;
    }

    @Transactional
    public PortResponseDTO addPort(PortAddRequestDTO request, UUID stationId) {

        Station station = stationRepo.findById(stationId).orElseThrow(()-> new RessourceNotFound("station with this id not found"));

        Port port = Port.builder()
                .name(request.getPortName())
                .station(station)
                .createdAt(Instant.now())
                .status(PortStatus.AVAILABLE)
                .build();
        portRepo.save(port);

        return PortResponseDTO.builder()
                .portName(port.getName())
                .portId(port.getId())
                .stationId(station.getStationId())
                .portStatus(port.getStatus())
                .accessIdentifier(port.getAccessIdentifier())
                .build();



    }
}
