package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.StationDTO.StationRequestDTO;
import com.reservationSys.reservationSys.DTOs.StationDTO.StationDetailedResponseDTO;
import com.reservationSys.reservationSys.DTOs.StationDTO.StationResponseDTO;
import com.reservationSys.reservationSys.Services.Station.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping("add-station")
    public ResponseEntity<StationResponseDTO> addStation(StationRequestDTO request){
        return ResponseEntity.ok(stationService.addStation(request));
    }

    @GetMapping
    public ResponseEntity<List<StationResponseDTO>> getStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponseDTO> getStation(@PathVariable UUID id){

        return ResponseEntity.ok(stationService.getStation(id));
    }

    @GetMapping("/{id}/ports")
    public ResponseEntity<StationDetailedResponseDTO> getStationAndPorts(@PathVariable UUID id) {
        return ResponseEntity.ok(stationService.getStationAndPorts(id));
    }

    @GetMapping("/close")

    @DeleteMapping("/{id}")
    public ResponseEntity<StationResponseDTO> deleteStation(@PathVariable UUID id){
        return ResponseEntity.ok(stationService.deleteStation(id));
    }
}
