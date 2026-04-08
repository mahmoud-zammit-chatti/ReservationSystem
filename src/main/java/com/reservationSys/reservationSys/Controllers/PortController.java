package com.reservationSys.reservationSys.Controllers;

import com.reservationSys.reservationSys.DTOs.PortDTOs.PortAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortUpdateRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.TimeSlotsDTO;
import com.reservationSys.reservationSys.Models.reservation.Duration;
import com.reservationSys.reservationSys.Services.Port.PortService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PortController {

    private final PortService portService;


    public PortController(PortService portService) {
        this.portService = portService;
    }

    @PostMapping("/stations/{stationId}/ports")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<PortResponseDTO> addPort(@Valid @RequestBody PortAddRequestDTO request, @PathVariable UUID stationId) {
        return ResponseEntity.ok(portService.addPort(request,stationId));
    }

    @GetMapping("/stations/{stationId}/ports")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<List<PortResponseDTO>> getAllPorts(@PathVariable UUID stationId){

        return ResponseEntity.ok(portService.getPort(stationId));
    }

    @PutMapping("/stations/{stationId}/ports/{portId}")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<PortResponseDTO>  updatePortStatus ( @Valid @RequestBody PortUpdateRequestDTO request, @PathVariable UUID stationId,@PathVariable UUID portId){
        return ResponseEntity.ok(portService.updatePort(request,stationId,portId));
    }

    @DeleteMapping("/stations/{stationId}/ports/{portId}")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<PortResponseDTO> deletePort(@Valid @RequestBody PortUpdateRequestDTO request, @PathVariable UUID stationId,@PathVariable UUID portId){
        return ResponseEntity.ok(portService.deletePort(request,stationId,portId));
    }

    //user specific endpoints

    @GetMapping("/stations/{stationId}/ports/{portId}/slots/{date}/duration/{duration}")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<List<TimeSlotsDTO>> getAvailablePortsForUser(@PathVariable UUID stationId, @PathVariable UUID portId, @PathVariable LocalDate date, @PathVariable Duration duration){
        return ResponseEntity.ok(portService.getAvailableTimeSlots(stationId,portId,date,duration));
    }

    @GetMapping("/stations/{stationId}/ports/date/{date}/time/{startingTime}/duration/{duration}")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<List<PortResponseDTO>> getAvailablePortsForUser(@PathVariable UUID stationId, @PathVariable LocalDate date, @PathVariable int startingTime, @PathVariable Duration duration){
        return ResponseEntity.ok(portService.getAvailablePorts(stationId,date,startingTime,duration));
    }

}
