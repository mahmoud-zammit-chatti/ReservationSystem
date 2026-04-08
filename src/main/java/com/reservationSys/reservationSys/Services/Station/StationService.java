package com.reservationSys.reservationSys.Services.Station;


import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.DTOs.StationDTOs.*;
import com.reservationSys.reservationSys.Models.port.Port;
import com.reservationSys.reservationSys.Models.station.Station;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.StationRepo;
import com.reservationSys.reservationSys.Repositories.StationWithDistanceProjection;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.RessourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StationService {

    private final StationRepo stationRepo;
    private final PortRepo portRepo;

    public StationService(StationRepo stationRepo, PortRepo portRepo) {
        this.stationRepo = stationRepo;
        this.portRepo = portRepo;
    }

    @Transactional
    public StationResponseDTO addStation(StationRequestDTO request) {

        Station station = Station.builder()
                .name(request.getStationName())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .city(request.getCity())
                .town(request.getTown())
                .createdAt(Instant.now())
                .build();

        stationRepo.save(station);

        return StationResponseDTO.builder()
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .stationId(station.getStationId())
                .stationName(station.getName())
                .cityName(station.getCity())
                .townName(station.getTown())
                .createdAt(station.getCreatedAt())
                .build();

    }

    @Transactional
    public StationResponseDTO getStation(UUID id) {
        Station station = stationRepo.findById(id).orElseThrow(()-> new RessourceNotFound("station with this this not found"));

        return StationResponseDTO.builder()
                .stationId(station.getStationId())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .stationName(station.getName())
                .cityName(station.getCity())
                .townName(station.getTown())
                .createdAt(station.getCreatedAt())
                .build();
    }

    @Transactional
    public StationResponseDTO deleteStation(UUID id) {
        Station deleted = stationRepo.deleteByStationId(id).orElseThrow(()-> new RessourceNotFound("station with this this not found"));

        return StationResponseDTO.builder()
                .stationId(deleted.getStationId())
                .latitude(deleted.getLatitude())
                .longitude(deleted.getLongitude())
                .stationName(deleted.getName())
                .cityName(deleted.getCity())
                .townName(deleted.getTown())
                .createdAt(deleted.getCreatedAt())
                .build();
    }

    @Transactional
    public List<StationResponseDTO> getAllStations() {
        List<Station> stations = stationRepo.findAll();
        List<StationResponseDTO> response = new ArrayList<>();
        for (Station station : stations) {
            response.add(StationResponseDTO.builder()
                    .stationId(station.getStationId())
                    .latitude(station.getLatitude())
                    .longitude(station.getLongitude())
                    .stationName(station.getName())
                    .cityName(station.getCity())
                    .townName(station.getTown())
                    .createdAt(station.getCreatedAt())
                    .build());
        }
        return response;
    }
    @Transactional
    public StationDetailedResponseDTO getStationAndPorts(UUID stationId){

        StationResponseDTO station = getStation(stationId);
        List<Port> ports = portRepo.findAllByStation_StationId(stationId);

        return StationDetailedResponseDTO.builder()
                .station(station)
                .ports(ports)
                .build();

    }

    @Transactional
    public List<StationNearMeResponseDTO> getStationsInRadius(StationNearMeRequestDTO request) {

        List<StationWithDistanceProjection> rows= stationRepo.findStationWithinRadius(request.getRadius(),request.getLongitude(),request.getLatitude());
        List<StationNearMeResponseDTO> response = new ArrayList<>();

        for(StationWithDistanceProjection row: rows){

                if (!request.isAvailableOnly()) {
                    insertInResponse(row, response, null, row.getDistanceMeters());
                } else {

                    Instant endTime = request.getStartTime().plus(request.getDuration().getHours(), ChronoUnit.HOURS);

                    List<Port> availablePorts = portRepo.findAllByStation_StationIdAndIsAvailableTrue(row.getStationId(), request.getStartTime(), endTime);
                    List<PortResponseDTO> availablePortsDTO = new ArrayList<>();
                    for(Port p:availablePorts){
                        availablePortsDTO.add(
                                PortResponseDTO.builder()
                                        .portId(p.getId())
                                        .portName(p.getName())
                                        .stationId(p.getStation().getStationId())
                                        .portStatus(p.getStatus())
                                        .accessIdentifier(p.getAccessIdentifier()).build()
                        );
                    }
                    if (!availablePorts.isEmpty()) {

                        insertInResponse(row, response, availablePortsDTO,row.getDistanceMeters());
                    }
                }

        }

        return response;
    }

    @Transactional
    protected void insertInResponse(StationWithDistanceProjection st, List<StationNearMeResponseDTO> response, List<PortResponseDTO> availablePortsDTO, double distance) {

        List<Port> allPort = portRepo.findAllByStation_StationId(st.getStationId());
        List<PortResponseDTO> allPortsDTO = new ArrayList<>();
        for(Port p:allPort){
            allPortsDTO.add(
                    PortResponseDTO.builder()
                            .portId(p.getId())
                            .portName(p.getName())
                            .stationId(p.getStation().getStationId())
                            .portStatus(p.getStatus())
                            .accessIdentifier(p.getAccessIdentifier())
                            .build()
            );
        }
        response.add(
                StationNearMeResponseDTO.builder()
                        .allPorts(allPortsDTO)
                        .availablePortsForSLot(availablePortsDTO)
                        .distance(distance)
                        .stationName(st.getName())
                        .latitude(st.getLatitude())
                        .longitude(st.getLongitude())
                        .stationId(st.getStationId()).build()
        );
    }

}



