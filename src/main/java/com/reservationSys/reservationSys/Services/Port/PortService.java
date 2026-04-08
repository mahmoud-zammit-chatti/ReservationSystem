package com.reservationSys.reservationSys.Services.Port;


import com.reservationSys.reservationSys.DTOs.PortDTOs.PortAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortUpdateRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.TimeSlotsDTO;
import com.reservationSys.reservationSys.Models.port.Port;
import com.reservationSys.reservationSys.Models.port.PortStatus;
import com.reservationSys.reservationSys.Models.reservation.Duration;
import com.reservationSys.reservationSys.Models.reservation.Reservation;
import com.reservationSys.reservationSys.Models.reservation.ReservationStatus;
import com.reservationSys.reservationSys.Models.station.Station;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.ReservationRepo;
import com.reservationSys.reservationSys.Repositories.StationRepo;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.RessourceNotFound;
import com.reservationSys.reservationSys.Exceptions.PortExceptions.PortCantBeDeletedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Reporter.log;

@Slf4j
@Service
public class PortService {

    private final StationRepo stationRepo;
    private final PortRepo portRepo;
    private final ReservationRepo reservationRepo;
    private final PortStatusUpdateService portStatusUpdateService;
    private final PortAvailabilityCheckService availabilityChecker;

    //unifed time
    private final ZoneId businessZoneId;
    private final Clock businessClock;


    private final List<ReservationStatus> ACTIVESTATUS = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN, ReservationStatus.PENDING_OTP);


    public PortService(StationRepo stationRepo, PortRepo portRepo, ReservationRepo reservationRepo, PortStatusUpdateService portStatusUpdateService, PortAvailabilityCheckService availabilityChecker, ZoneId businessZoneId, Clock businessClock) {
        this.stationRepo = stationRepo;
        this.portRepo = portRepo;
        this.reservationRepo = reservationRepo;
        this.portStatusUpdateService = portStatusUpdateService;
        this.availabilityChecker = availabilityChecker;
        this.businessZoneId = businessZoneId;
        this.businessClock = businessClock;
    }

    @Transactional
    public PortResponseDTO addPort(PortAddRequestDTO request, UUID stationId) {

        Station station = stationRepo.findById(stationId).orElseThrow(() -> new RessourceNotFound("station with this id not found"));

        Port port = Port.builder()
                .name(request.getPortName())
                .station(station)
                .createdAt(Instant.now(businessClock))
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

    @Transactional
    public List<PortResponseDTO> getPort(UUID stationId) {

        List<Port> ports = portRepo.findAllByStation_StationId(stationId);

        List<PortResponseDTO> portsDTO = new ArrayList<>();
        for (Port p : ports) {
            portsDTO.add(
                    PortResponseDTO.builder()
                            .portName(p.getName())
                            .portId(p.getId())
                            .stationId(p.getStation().getStationId())
                            .portStatus(p.getStatus())
                            .accessIdentifier(p.getAccessIdentifier())
                            .build()
            );
        }
        return portsDTO;
    }

    @Transactional
    public PortResponseDTO updatePort(PortUpdateRequestDTO request, UUID stationId, UUID portId) {
        Port port = portRepo.findByIdAndStation_StationId(portId, stationId).orElseThrow(() -> new RessourceNotFound("can't find the requested port"));

        port.setName(request.getNewName());
        portRepo.save(port);
        return PortResponseDTO.builder()
                .portName(port.getName())
                .portStatus(port.getStatus())
                .portId(portId)
                .stationId(stationId)
                .accessIdentifier(port.getAccessIdentifier())
                .build();
    }


    @Transactional
    public PortResponseDTO deletePort(PortUpdateRequestDTO request, UUID stationId, UUID portId) {

        //first check if there are any reservations fot this port
        // if yes it can't be deleted but its status will be updated to expiring_soon and then it can't accept any
        // new reservations and set a scheduled job to delete the port when its reservations passed

        List<Reservation> reservationList = reservationRepo.findAllByPortIdAndReservationStatusIn(portId, ACTIVESTATUS);
        if (!reservationList.isEmpty()) {

            portStatusUpdateService.updatePortStatus(portId, stationId);

            throw new PortCantBeDeletedException("this port have one or more active reservation, it has been marked to be deleted as soon as no reservation is active on it and it can't accept any more reservation");
        } else {

            Port port = portRepo.deleteByIdAndStation_StationId(portId, stationId).orElseThrow(() -> new RessourceNotFound("can't find the requested port"));

            return PortResponseDTO.builder()
                    .portName(port.getName())
                    .portStatus(port.getStatus())
                    .portId(portId)
                    .stationId(stationId)
                    .accessIdentifier(port.getAccessIdentifier())
                    .build();
        }

    }

    @Transactional
    @Scheduled(fixedDelayString = "${port_deletion_scheduler_interval}")
    public void deletePortsOnSchedule() {
        List<Port> portList = portRepo.PortsToBeDeleted();

        portRepo.deleteAll(portList);

        if (!portList.isEmpty()) {
            log("Some ports were deleted by a scheduled service !");
        }

    }

    @Transactional
    public List<TimeSlotsDTO> getAvailableTimeSlots(UUID stationId, UUID portId, LocalDate date, Duration duration) {


        Port port = portRepo.findByIdAndStation_StationId(portId, stationId).orElseThrow(() -> new RessourceNotFound("can't find the requested port"));

        Instant dayStart = date.atStartOfDay(businessZoneId).toInstant();


        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        List<String> statuses = ACTIVESTATUS.stream().map(Enum::name).toList();

        List<Reservation> reservationList = reservationRepo.findByPortStationAndDate(portId, stationId, statuses, dayStart, dayEnd);


        List<TimeSlotsDTO> result = new ArrayList<>();

        for (int i = 0; i <= 23; i++) {
            Instant candidateStart = date.atTime(i, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant candidateEnd = candidateStart.plus(duration.getHours(), ChronoUnit.HOURS);
            if (availabilityChecker.isTimeSLotAvailable(candidateStart, candidateEnd, reservationList)) {
                result.add(
                        TimeSlotsDTO.builder()
                                .portId(portId)
                                .portName(port.getName())
                                .startTime(i)
                                .endTime(candidateEnd.atZone(businessZoneId).getHour())
                                .build()
                );
            }
        }

        return result;
    }


    public List<PortResponseDTO> getAvailablePorts(UUID stationId, LocalDate date, int startingTime, Duration duration) {

        List<PortResponseDTO> responseDTOS = new ArrayList<>();

        List<Port> portList = portRepo.findAllByStation_StationId(stationId);
        Instant candidateStart = date.atTime(startingTime, 0, 0, 0).atZone(businessZoneId).toInstant();
        Instant candidateEnd = candidateStart.plus(duration.getHours(), ChronoUnit.HOURS);

        for (Port p : portList) {
            List<Reservation> reservationList = reservationRepo.findByPortStationAndDate(p.getId(), stationId, ACTIVESTATUS.stream().map(Enum::name).toList(), candidateStart, candidateEnd);
            if (availabilityChecker.isTimeSLotAvailable(candidateStart, candidateEnd, reservationList)) {
                responseDTOS.add(PortResponseDTO.builder()
                        .portName(p.getName())
                        .portId(p.getId())
                        .stationId(stationId)
                        .portStatus(p.getStatus())
                        .accessIdentifier(p.getAccessIdentifier())
                        .build());


            }
        }
        return responseDTOS;
    }
}
