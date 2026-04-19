package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationConfirmationDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationResponseDTO;
import com.reservationSys.reservationSys.Models.reservation.CancellationReason;
import com.reservationSys.reservationSys.Models.reservation.ReservationStatus;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Services.Reservation.ReservationService;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ReservationResponseDTO> addReservation(@Valid @ModelAttribute ReservationAddRequestDTO requestDTO, @AuthenticationPrincipal MyAppUserDetails userDetails) {
        AppUser user = userDetails.getAppUser();
        return ResponseEntity.ok(reservationService.addReservation(requestDTO, user.getId()));
    }

    @PutMapping("/confirmation/{reservationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(@PathVariable UUID reservationId, @AuthenticationPrincipal MyAppUserDetails userDetails,@Valid @ModelAttribute ReservationConfirmationDTO requestDTO) {
        AppUser user = userDetails.getAppUser();
        return ResponseEntity.ok(reservationService.confirmReservation(reservationId, requestDTO, user.getId()));
    }

    @PostMapping("/resend-confirmation-request/{reservationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> resendConfirmationRequest(@PathVariable UUID reservationId, @AuthenticationPrincipal MyAppUserDetails userDetails) {
        AppUser user = userDetails.getAppUser();
        reservationService.resendConfirmationRequest(reservationId, user.getId());
        return ResponseEntity.status(200).body("An OTP was sent to the number associated with the reservation :)");
    }

    @GetMapping("/getAll/status/{status}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservationByStatus(@AuthenticationPrincipal MyAppUserDetails userDetails, @PathVariable ReservationStatus status){
        AppUser appUser = userDetails.getAppUser();
        return ResponseEntity.ok(reservationService.getAllReservationsByStatus(appUser.getId(),status));
    }
    @GetMapping("/getAll")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservationByStatus(@AuthenticationPrincipal MyAppUserDetails userDetails){
        AppUser appUser = userDetails.getAppUser();
        return ResponseEntity.ok(reservationService.getAllReservations(appUser.getId()));
    }

    @GetMapping("/{reservationId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ReservationResponseDTO> getReservation(@AuthenticationPrincipal MyAppUserDetails userDetails,@PathVariable UUID reservationId){
        AppUser appUser = userDetails.getAppUser();

        return ResponseEntity.ok(reservationService.getReservation(reservationId,appUser.getId()));
    }

    @GetMapping("/getAll/car/{carId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservationsByCarId(@AuthenticationPrincipal MyAppUserDetails userDetails,@PathVariable UUID carId) {
        return ResponseEntity.ok(reservationService.getAllReservationsByCarId(userDetails.getAppUser().getId(),carId));
    }


    @PutMapping("cancel/reservation/{reservationId}/reason/{reason}")//reason is set by the front end dev user_cancel or car_removed
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteReservation(@AuthenticationPrincipal MyAppUserDetails userDetails,@PathVariable UUID reservationId,@PathVariable CancellationReason reason){
        AppUser appUser = userDetails.getAppUser();
        reservationService.cancelReservation(reservationId,appUser.getId(),reason);
        return ResponseEntity.ok("reservation cancelled, check cancellation fee and penalty ");
    }




}