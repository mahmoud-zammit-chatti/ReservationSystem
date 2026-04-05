package com.reservationSys.reservationSys.Controllers;

import com.reservationSys.reservationSys.DTOs.PortDTOs.PortAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortUpdateRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.TimeSlotsDTO;
import com.reservationSys.reservationSys.Domain.reservation.Duration;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Services.Port.PortService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PortController {

    private final PortRepo portRepo;
    private final PortService portService;


    public PortController(PortRepo portRepo, PortService portService) {
        this.portRepo = portRepo;
        this.portService = portService;
    }

    @PostMapping("/stations/{stationId}/ports")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<PortResponseDTO> addPort(@RequestBody PortAddRequestDTO request, @PathVariable UUID stationId) {
        return ResponseEntity.ok(portService.addPort(request,stationId));
    }

    @GetMapping("/stations/{stationId}/ports")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<List<PortResponseDTO>> getAllPorts(@PathVariable UUID stationId){

        return ResponseEntity.ok(portService.getPort(stationId));
    }

    @PutMapping("/stations/{stationId}/ports/{portId}")
    @SecurityRequirement(name="Bearer Authentication")

    public ResponseEntity<PortResponseDTO>  updatePortStatus(@RequestBody PortUpdateRequestDTO request, @PathVariable UUID stationId,@PathVariable UUID portId){
        return ResponseEntity.ok(portService.updatePort(request,stationId,portId));
    }

    @DeleteMapping("/stations/{stationId}/ports/{portId}")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<PortResponseDTO> deletePort(@RequestBody PortUpdateRequestDTO request, @PathVariable UUID stationId,@PathVariable UUID portId){
        return ResponseEntity.ok(portService.deletePort(request,stationId,portId));
    }

    //user specific endpoints

    @GetMapping("/stations/{stationId}/ports/{portId}/slots/{date}/duration/{duration}")
    @SecurityRequirement(name="Bearer Authentication")
    public ResponseEntity<List<TimeSlotsDTO>> getAvailablePortsForUser(@PathVariable UUID stationId, @PathVariable UUID portId, @PathVariable LocalDate date, @PathVariable Duration duration){
        return ResponseEntity.ok(portService.getAvailableTimeSlots(stationId,portId,date,duration));
    }

}
