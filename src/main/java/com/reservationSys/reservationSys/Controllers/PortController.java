package com.reservationSys.reservationSys.Controllers;

import com.reservationSys.reservationSys.DTOs.PortDTOs.PortAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.PortDTOs.PortResponseDTO;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Services.Port.PortService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
