package com.reservationSys.reservationSys.Services.Station;


import com.reservationSys.reservationSys.DTOs.StationDTO.*;
import com.reservationSys.reservationSys.Domain.port.Port;
import com.reservationSys.reservationSys.Domain.station.Station;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.StationRepo;
import com.reservationSys.reservationSys.Repositories.StationWithDistanceProjection;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.stereotype.Service;

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
    public StationDetailedResponseDTO getStationAndPorts(UUID stationId){

        StationResponseDTO station = getStation(stationId);
        List<Port> ports = portRepo.findAllByStation_StationId(stationId);

        return StationDetailedResponseDTO.builder()
                .station(station)
                .ports(ports)
                .build();

    }

    public List<StationNearMeResponseDTO> getStationsInRadius(StationNearMeRequestDTO request) {

        List<StationWithDistanceProjection> rows= stationRepo.findStationWithinRadius(request.getRadius(),request.getLongitude(),request.getLatitude());
        List<StationNearMeResponseDTO> response = new ArrayList<>();

        for(StationWithDistanceProjection row: rows){

                if (!request.isAvailableOnly()) {
                    insertInResponse(row, response, null, row.getDistanceMeters());
                } else {

                    Instant endTime = request.getStartTime().plus(request.getDuration().getHours(), ChronoUnit.HOURS);

                    List<Port> availablePorts = portRepo.findAllByStation_StationIdAndIsAvailableTrue(row.getStationId(), request.getStartTime(), endTime);
                    if (!availablePorts.isEmpty()) {

                        insertInResponse(row, response, availablePorts,row.getDistanceMeters());
                    }
                }

        }

        return response;
    }

    private void insertInResponse(StationWithDistanceProjection st, List<StationNearMeResponseDTO> response, List<Port> availablePorts, double distance) {
        response.add(
                StationNearMeResponseDTO.builder()
                        .allPorts(portRepo.findAllByStation_StationId(st.getStationId()))
                        .availablePortsForSLot(availablePorts)
                        .distance(distance)
                        .stationName(st.getName())
                        .latitude(st.getLatitude())
                        .longitude(st.getLongitude())
                        .stationId(st.getStationId()).build()
        );
    }

}



