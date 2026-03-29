package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.StationDTOs.*;
import com.reservationSys.reservationSys.Services.Station.StationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<StationResponseDTO> addStation(StationRequestDTO request){
        return ResponseEntity.ok(stationService.addStation(request));
    }
    @SecurityRequirement(name="Bearer Authentication")

    @GetMapping
    public ResponseEntity<List<StationResponseDTO>> getStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }
    @SecurityRequirement(name="Bearer Authentication")

    @GetMapping("/{id}")
    public ResponseEntity<StationResponseDTO> getStation(@PathVariable UUID id){

        return ResponseEntity.ok(stationService.getStation(id));
    }
    @SecurityRequirement(name="Bearer Authentication")

    @GetMapping("/{id}/ports")
    public ResponseEntity<StationDetailedResponseDTO> getStationAndPorts(@PathVariable UUID id) {
        return ResponseEntity.ok(stationService.getStationAndPorts(id));
    }
    @SecurityRequirement(name="Bearer Authentication")

    @GetMapping("/near-me")
    public ResponseEntity<List<StationNearMeResponseDTO>> getStationNearMe(@ModelAttribute StationNearMeRequestDTO request) {
        return ResponseEntity.ok(stationService.getStationsInRadius(request));
    }
    @SecurityRequirement(name="Bearer Authentication")

    @DeleteMapping("/{id}")
    public ResponseEntity<StationResponseDTO> deleteStation(@PathVariable UUID id){
        return ResponseEntity.ok(stationService.deleteStation(id));
    }
}
