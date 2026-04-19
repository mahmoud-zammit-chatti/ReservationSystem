package com.reservationSys.reservationSys.Services.Reservation;


import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationConfirmationDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationResponseDTO;
import com.reservationSys.reservationSys.Models.car.Car;
import com.reservationSys.reservationSys.Models.car.CarStatus;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.port.Port;
import com.reservationSys.reservationSys.Models.port.PortStatus;
import com.reservationSys.reservationSys.Models.reservation.CancellationReason;
import com.reservationSys.reservationSys.Models.reservation.Reservation;
import com.reservationSys.reservationSys.Models.reservation.ReservationStatus;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.ReservationRepo;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.Services.auth.EmailService;
import com.reservationSys.reservationSys.Exceptions.CarExceptions.CarNotVerifiedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.NotAuthorizedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import com.reservationSys.reservationSys.Exceptions.PortExceptions.PortNotAvailableException;
import com.reservationSys.reservationSys.Exceptions.ReservationExceptions.ReservationCancellationException;
import com.reservationSys.reservationSys.Exceptions.ReservationExceptions.ReservationConfirmationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.reservationSys.reservationSys.Models.reservation.PenaltyType.LATE_CANCEL_PENALTY;
import static com.reservationSys.reservationSys.Models.reservation.PenaltyType.NO_PENALTY;

@Service
public class ReservationService {

    private final ReservationRepo reservationRepo;
    private final PortRepo portRepo;
    private final CarRepo carRepo;
    private final OtpService otpService;
    private final TwilioService twilioService;

    private final ZoneId buisnessZoneId;
    private final Clock buidnessClock;

    private final List<ReservationStatus> ACTIVESTATUS = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN, ReservationStatus.PENDING_OTP);


    public ReservationService(ReservationRepo reservationRepo, PortRepo portRepo, CarRepo carRepo, OtpService otpService, TwilioService twilioService, ZoneId buisnessZoneId, Clock buidnessClock) {
        this.reservationRepo = reservationRepo;
        this.portRepo = portRepo;
        this.carRepo = carRepo;
        this.otpService = otpService;
        this.twilioService = twilioService;
        this.buisnessZoneId = buisnessZoneId;
        this.buidnessClock = buidnessClock;

    }



    @Transactional
    public ReservationResponseDTO addReservation(ReservationAddRequestDTO requestDTO, UUID userId) {

        Port port = portRepo.findById(requestDTO.getPortId()).orElseThrow(() -> new ResourceNotFound("Port not found"));
        Car car = carRepo.findById(requestDTO.getCarId()).orElseThrow(() -> new ResourceNotFound("Car not found"));

        if(!car.getUserId().equals(userId)){
            throw new NotAuthorizedException("You don't own this resource!!");
        }

        if (car.getStatus() != CarStatus.VERIFIED) {
            throw new CarNotVerifiedException("Car is not verified please request for verification before making a reservation");
        }

        //checking if port is available for the requested duration
        if (port.getStatus() != PortStatus.AVAILABLE) {
            throw new PortNotAvailableException("Port is currently not available for reservation, please choose another port or try again later :)");
        }
        Instant candidateStartTime = requestDTO.getStartDate().atTime(requestDTO.getStartTimeHour(), 0, 0, 0).atZone(buisnessZoneId).toInstant();
        Instant candidateEndTime = candidateStartTime.plus(requestDTO.getDuration().getHours(), ChronoUnit.HOURS);


        List<Reservation> reservationList = reservationRepo.findConflictedReservations(port.getId(), candidateStartTime, candidateEndTime, ACTIVESTATUS.stream().map(Enum::name).toList());
        if (!reservationList.isEmpty()) {
            throw new PortNotAvailableException("Port not available for the selected time slot please chose another :)");
        }


        Reservation newReservation = Reservation.builder()
                .reservationStatus(ReservationStatus.PENDING_OTP)
                .carId(car.getId())
                .portId(port.getId())
                .createdAt(Instant.now(buidnessClock))

                .startTime(candidateStartTime)
                .endTime(candidateEndTime)
                .duration(requestDTO.getDuration())
                .contactNumber(requestDTO.getContactNumber())
                .lateCancel(false)
                .penaltyWaived(false)
                .userId(userId)
                .build();

        reservationRepo.save(newReservation);


        //confirmation code with SMS
        String otpCode= otpService.generateOtpForReservation(newReservation.getId(), userId, OtpPurpose.RESERVATION_CONFIRMATION);
        twilioService.sendSms("+216"+requestDTO.getContactNumber(), otpCode);

        //there is the option to send with email ;
        //emailService.sendVerificationEmail(user.getEmail(), otpCode,"Reservation verification for E-Car reservation system: ");


        return ReservationResponseDTO.builder()
                .carId(newReservation.getCarId())
                .userId(newReservation.getUserId())
                .status(newReservation.getReservationStatus())
                .contactNumber(newReservation.getContactNumber())
                .duration(newReservation.getDuration())
                .startDate(newReservation.getStartTime().atZone(buisnessZoneId).toLocalDate())
                .endTime((newReservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                .startTime((newReservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                .portId(newReservation.getPortId())
                .reservationId(newReservation.getId())
                .build();

    }

    @Transactional
    public ReservationResponseDTO confirmReservation(UUID reservationId, ReservationConfirmationDTO requestDTO, UUID userId) {

        Reservation reservation = reservationRepo.findByIdAndUserId(reservationId,userId).orElseThrow(() -> new ResourceNotFound("Reservation not found"));

        if(reservation.getReservationStatus().equals(ReservationStatus.CONFIRMED)){
            throw new ReservationConfirmationException("Reservation is already confirmed :)");
        }

        boolean verifiedOtp=otpService.verifyOtp(reservationId, requestDTO.getCode(), OtpPurpose.RESERVATION_CONFIRMATION);

        if(verifiedOtp){
            reservation.setReservationStatus(ReservationStatus.CONFIRMED);
            reservationRepo.save(reservation);
            return ReservationResponseDTO.builder()
                    .carId(reservation.getCarId())
                    .userId(reservation.getUserId())
                    .status(reservation.getReservationStatus())
                    .contactNumber(reservation.getContactNumber())
                    .duration(reservation.getDuration())
                    .endTime((reservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                    .startTime((reservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                    .portId(reservation.getPortId())
                    .startDate(reservation.getStartTime().atZone(buisnessZoneId).toLocalDate())
                    .reservationId(reservation.getId())
                    .build();
        }else{

            //in the future maybe i'll add a 3 times try limit and after that make the otp status INVALIDATED and he must request for a new code(in a 2 min window abuse prevention sys)
            throw new ReservationConfirmationException("the code you entered is not matching the one you received, Please check the code and try again");

        }




    }

    @Transactional
    public void resendConfirmationRequest(UUID reservationId, UUID userId) {
        Reservation reservation = reservationRepo.findByIdAndUserId(reservationId,userId).orElseThrow(() -> new ResourceNotFound("Reservation not found"));



        if(!reservation.getReservationStatus().equals(ReservationStatus.PENDING_OTP)){
            throw new ReservationConfirmationException("This reservation is not pending confirmation, you can't resend a confirmation request for it");
        }
        String code = otpService.generateOtpForReservation(reservation.getId(), userId, OtpPurpose.RESERVATION_CONFIRMATION);
        twilioService.sendSms("+216"+reservation.getContactNumber(), "A new OTP code was generated for your reservation, please check the code and confirm your reservation :) \nCODE: "+code);


    }

    @Transactional
    public List<ReservationResponseDTO> getAllReservations(UUID userId) {

        List<Reservation> reservationList = reservationRepo.findAllByUserId(userId);
        List<ReservationResponseDTO> responseDTOS=new ArrayList<>();

        reservationList.forEach( (reservation)-> responseDTOS.add(
                ReservationResponseDTO.builder()
                    .carId(reservation.getCarId())
                    .userId(reservation.getUserId())
                    .status(reservation.getReservationStatus())
                    .contactNumber(reservation.getContactNumber())
                        .startDate(reservation.getStartTime().atZone(buisnessZoneId).toLocalDate())
                    .duration(reservation.getDuration())
                    .endTime((reservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                    .startTime((reservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                    .portId(reservation.getPortId())
                    .reservationId(reservation.getId())
                    .build())
        );

        return responseDTOS;
    }

    @Transactional
    public ReservationResponseDTO getReservation(UUID resId,UUID userId){
        Reservation reservation = reservationRepo.findByIdAndUserId(resId,userId).orElseThrow(() -> new ResourceNotFound("Reservation not found"));



        return ReservationResponseDTO.builder()
                .carId(reservation.getCarId())
                .userId(reservation.getUserId())
                .status(reservation.getReservationStatus())
                .contactNumber(reservation.getContactNumber())
                .duration(reservation.getDuration())
                .endTime((reservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                .startTime((reservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                .portId(reservation.getPortId())
                .startDate(reservation.getStartTime().atZone(buisnessZoneId).toLocalDate())

                .reservationId(reservation.getId())
                .build();
    }

    @Transactional
    public List<ReservationResponseDTO> getAllReservationsByStatus(UUID userId,ReservationStatus status) {
        List<Reservation> reservationList = reservationRepo.findAllByUserIdAndReservationStatusEquals(userId,status);
        List<ReservationResponseDTO> responseDTOS=new ArrayList<>();

        reservationList.forEach( (reservation)-> responseDTOS.add(
                ReservationResponseDTO.builder()
                        .carId(reservation.getCarId())
                        .userId(reservation.getUserId())
                        .status(reservation.getReservationStatus())
                        .contactNumber(reservation.getContactNumber())
                        .duration(reservation.getDuration())
                        .endTime((reservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                        .startTime((reservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                        .startDate(reservation.getStartTime().atZone(buisnessZoneId).toLocalDate())
                        .portId(reservation.getPortId())
                        .reservationId(reservation.getId())
                        .build())
        );

        return responseDTOS;



    }

    @Transactional
    public List<ReservationResponseDTO> getAllReservationsByCarId(UUID userId, UUID carId) {

        List<Reservation> reservationList = reservationRepo.findAllByUserIdAndCarId(userId,carId);
        List<ReservationResponseDTO> responseDTOS=new ArrayList<>();

        reservationList.forEach( (reservation)-> responseDTOS.add(
                ReservationResponseDTO.builder()
                        .carId(reservation.getCarId())
                        .userId(reservation.getUserId())
                        .status(reservation.getReservationStatus())
                        .contactNumber(reservation.getContactNumber())
                        .duration(reservation.getDuration())
                        .startDate(reservation.getStartTime().atZone(buisnessZoneId).toLocalDate())
                        .endTime((reservation.getEndTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                        .startTime((reservation.getStartTime()).atZone(buisnessZoneId).toLocalDateTime().getHour())
                        .portId(reservation.getPortId())
                        .reservationId(reservation.getId())
                        .build())
        );

        return responseDTOS;

    }

    public void cancelReservation(UUID reservationId, UUID userId,CancellationReason reason) {

        Reservation reservation = reservationRepo.findByIdAndUserId(reservationId,userId).orElseThrow(() -> new ResourceNotFound("Reservation not found"));

        if(reservation.getReservationStatus()==ReservationStatus.CANCELLED){
            throw new ReservationCancellationException("This reservation is already cancelled :(");
        }

        List<ReservationStatus> statuses = List.of(ReservationStatus.CHECKED_IN, ReservationStatus.COMPLETED, ReservationStatus.EXPIRED, ReservationStatus.NO_SHOW);
        if(statuses.contains(reservation.getReservationStatus())){
            throw new ReservationCancellationException("You can't cancel a reservation with this status :(");
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(Instant.now(buidnessClock));
            reservation.setCancellationReason(reason);

        if(reservation.getCancelledAt().isAfter(reservation.getStartTime().minus(12, ChronoUnit.HOURS))){
            reservation.setLateCancel(true);
            reservation.setPenaltyType(LATE_CANCEL_PENALTY);

        }
        if(reservation.getCancelledAt().isBefore(reservation.getStartTime().minus(12, ChronoUnit.HOURS))){
            reservation.setLateCancel(false);
            reservation.setPenaltyWaived(true);
            reservation.setPenaltyType(NO_PENALTY);
        }
        reservationRepo.save(reservation);



    }
}
