package com.reservationSys.reservationSys.Services.Reservation;


import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationConfirmationDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationResponseDTO;
import com.reservationSys.reservationSys.Exceptions.CarExceptions.CarNotVerifiedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.NotAuthorizedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import com.reservationSys.reservationSys.Exceptions.PortExceptions.PortNotAvailableException;
import com.reservationSys.reservationSys.Exceptions.ReservationExceptions.ReservationConfirmationException;
import com.reservationSys.reservationSys.Models.car.Car;
import com.reservationSys.reservationSys.Models.car.CarStatus;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.port.Port;
import com.reservationSys.reservationSys.Models.port.PortStatus;
import com.reservationSys.reservationSys.Models.reservation.Duration;
import com.reservationSys.reservationSys.Models.reservation.Reservation;
import com.reservationSys.reservationSys.Models.reservation.ReservationStatus;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Repositories.CarRepo;
import com.reservationSys.reservationSys.Repositories.PortRepo;
import com.reservationSys.reservationSys.Repositories.ReservationRepo;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.Services.auth.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private PortRepo portRepo;
    @Mock
    private CarRepo carRepo;
    @Mock
    private ReservationRepo reservationRepo;
    @Mock
    private OtpService otpService;
    @Mock
    private TwilioService twilioService;
    @Mock
    private EmailService emailService;

    public final List<ReservationStatus> ACTIVESTATUS = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN, ReservationStatus.PENDING_OTP);

    private ReservationService reservationService;
    @BeforeEach
    void setUp() {

        ZoneId realZoneId = ZoneId.of("Africa/Tunis");
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());

        reservationService = new ReservationService(reservationRepo, portRepo, carRepo, otpService, twilioService, realZoneId, fixedClock);
    }

    //tests for adding new reservations
    @Test
    void addReservation_WhenHappyPath() {
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(user.getId(), CarStatus.VERIFIED);


        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);


        Instant instant = requestDTO.getStartDate().atTime(requestDTO.getStartTimeHour(), 0).atZone(ZoneId.of("Africa/Tunis")).toInstant();
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));

        when(reservationRepo.findConflictedReservations(port.getId(), instant, instant.plus(requestDTO.getDuration().getHours(), ChronoUnit.HOURS), ACTIVESTATUS.stream().map(Enum::name).toList())).thenReturn(List.of());

        when(otpService.generateOtpForReservation(any(), eq(user.getId()), eq(OtpPurpose.RESERVATION_CONFIRMATION)))
                .thenReturn("123456");

        ReservationResponseDTO responseDTO = reservationService.addReservation(requestDTO, user.getId());

        assertNotNull(responseDTO);
        assertEquals(port.getId(), responseDTO.getPortId());
        assertEquals(car.getId(), responseDTO.getCarId());
        assertEquals(ReservationStatus.PENDING_OTP, responseDTO.getStatus());


        verify(otpService, times(1)).generateOtpForReservation(any(), eq(user.getId()), eq(OtpPurpose.RESERVATION_CONFIRMATION));
        verify(twilioService, times(1)).sendSms(eq("+21612345678"), eq("123456"));
        verify(reservationRepo, times(1)).save(reservationCaptor.capture());

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(port.getId(), savedReservation.getPortId());
        assertEquals(car.getId(), savedReservation.getCarId());
        assertEquals(ReservationStatus.PENDING_OTP, savedReservation.getReservationStatus());
        assertEquals(user.getId(), savedReservation.getUserId());


    }

    @Test
    void addReservation_WhenPortIsNotAvailable() {

        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.OUT_OF_SERVICE);

        Car car = createCar(user.getId(), CarStatus.VERIFIED);

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        assertThrows(PortNotAvailableException.class, () -> reservationService.addReservation(requestDTO, user.getId()));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenUserDoesNotOwnCar() {
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(UUID.randomUUID(), CarStatus.VERIFIED); // Car owned by another user

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        assertThrows(NotAuthorizedException.class, () -> reservationService.addReservation(requestDTO, user.getId()));

        reservationAdditionStopped();

    }

    @Test
    void addReservation_WhenCarIsNotVerified() {
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(appUser.getId(), CarStatus.UNVERIFIED); // Car not verified

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));

        assertThrows(CarNotVerifiedException.class, () -> reservationService.addReservation(requestDTO, appUser.getId()));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenPortIsNotAvailableForTimeSlot() {
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(user.getId(), CarStatus.VERIFIED);


        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());


        ZoneId tunisiaId = ZoneId.of("Africa/Tunis");
        Instant instant = requestDTO.getStartDate().atTime(requestDTO.getStartTimeHour(), 0).atZone(tunisiaId).toInstant();
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        when(reservationRepo.findConflictedReservations(port.getId(), instant, instant.plus(requestDTO.getDuration().getHours(), ChronoUnit.HOURS), ACTIVESTATUS.stream().map(Enum::name).toList())).thenReturn(List.of(mock(Reservation.class)));

        assertThrows(PortNotAvailableException.class, () -> reservationService.addReservation(requestDTO, user.getId()));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenCarDoesNotExist() {
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(UUID.randomUUID(), CarStatus.VERIFIED); // Car that does not exist in the repo

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.empty()); // Car not found

        assertThrows(ResourceNotFound.class, () -> reservationService.addReservation(requestDTO, appUser.getId()));

        reservationAdditionStopped();

    }

    @Test
    void addReservation_WhenPortDoesNotExist() {
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE); // Port that does not exist in the repo

        Car car = createCar(appUser.getId(), CarStatus.VERIFIED);

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.empty()); // Port not found

        assertThrows(ResourceNotFound.class, () -> reservationService.addReservation(requestDTO, appUser.getId()));

        reservationAdditionStopped();

    }

    //Tests for confirmation

    @Test
    void confirmReservation_WhenReservationDoesNotExist(){
        AppUser user = createDefaultUser();

        Car car = createCar(user.getId(),CarStatus.VERIFIED);

        Port port = createPort(PortStatus.AVAILABLE);

        Reservation reservation = createReservation(user.getId(),car.getId(),port.getId(),ReservationStatus.PENDING_OTP);

        ReservationConfirmationDTO requestDTO =  createConfirmationRequest();

        when(reservationRepo.findByIdAndUserId(reservation.getId(),user.getId())).thenReturn(Optional.empty());//reservation not in repo

        assertThrows(ResourceNotFound.class,()->reservationService.confirmReservation(reservation.getId(),requestDTO,user.getId()));

        verifyNoInteractions(otpService);
        verify(reservationRepo,never()).save(any());
        verify(reservationRepo,times(1)).findByIdAndUserId(reservation.getId(),user.getId());
    }

    @Test
    void confirmReservation_WhenReservationIsAlreadyConfirmed(){

        AppUser user = createDefaultUser();

        Car car = createCar(user.getId(),CarStatus.VERIFIED);

        Port port = createPort(PortStatus.AVAILABLE);

        Reservation reservation = createReservation(user.getId(),car.getId(),port.getId(),ReservationStatus.CONFIRMED);

        ReservationConfirmationDTO requestDTO =  createConfirmationRequest();

        when(reservationRepo.findByIdAndUserId(reservation.getId(),user.getId())).thenReturn(Optional.of(reservation));//reservation in repo but already confirmed

        assertThrows(ReservationConfirmationException.class,()->reservationService.confirmReservation(reservation.getId(),requestDTO,user.getId()));

        verifyNoInteractions(otpService);
        verify(reservationRepo,never()).save(any());
        verify(reservationRepo,times(1)).findByIdAndUserId(reservation.getId(),user.getId());

    }

    @Test
    void confirmReservation_WhenEnteredCodeIsWrong(){
        AppUser user = createDefaultUser();
        Car car = createCar(user.getId(),CarStatus.VERIFIED);
        Port port = createPort(PortStatus.AVAILABLE);
        Reservation reservation = createReservation(user.getId(),car.getId(),port.getId(),ReservationStatus.PENDING_OTP);
        ReservationConfirmationDTO requestDTO =  createConfirmationRequest();


        when(reservationRepo.findByIdAndUserId(reservation.getId(),user.getId())).thenReturn(Optional.of(reservation)); //reservation in repo and pending_otp
        when(otpService.verifyOtp(reservation.getId(),requestDTO.getCode(), OtpPurpose.RESERVATION_CONFIRMATION)).thenReturn(false); //OTP verification fails

        assertThrows(ReservationConfirmationException.class,()->reservationService.confirmReservation(reservation.getId(),requestDTO,user.getId()));
        verify(reservationRepo,never()).save(any());
        verify(reservationRepo,times(1)).findByIdAndUserId(reservation.getId(),user.getId());
        verify(otpService,times(1)).verifyOtp(reservation.getId(),requestDTO.getCode(), OtpPurpose.RESERVATION_CONFIRMATION);

    }

    @Test
    void confirmReservation_WhenHappyPath(){
        AppUser user = createDefaultUser();
        Car car = createCar(user.getId(),CarStatus.VERIFIED);
        Port port = createPort(PortStatus.AVAILABLE);
        Reservation reservation = createReservation(user.getId(),car.getId(),port.getId(),ReservationStatus.PENDING_OTP);
        ReservationConfirmationDTO requestDTO =  createConfirmationRequest();

        ArgumentCaptor<Reservation> reservationArgumentCaptor = ArgumentCaptor.forClass(Reservation.class);


        when(reservationRepo.findByIdAndUserId(reservation.getId(),user.getId())).thenReturn(Optional.of(reservation)); //reservation in repo and pending_otp
        when(otpService.verifyOtp(reservation.getId(),requestDTO.getCode(), OtpPurpose.RESERVATION_CONFIRMATION)).thenReturn(true); //OTP verification is a success

        ReservationResponseDTO responseDTO =  reservationService.confirmReservation(reservation.getId(),requestDTO,user.getId());

        verify(reservationRepo,times(1)).save(reservationArgumentCaptor.capture());
        verify(reservationRepo,times(1)).findByIdAndUserId(reservation.getId(),user.getId());
        verify(otpService,times(1)).verifyOtp(reservation.getId(),requestDTO.getCode(), OtpPurpose.RESERVATION_CONFIRMATION);

        assertEquals(ReservationStatus.CONFIRMED,reservationArgumentCaptor.getValue().getReservationStatus());
        assertEquals(responseDTO.getReservationId(),reservation.getId());
    }

    // tests for resending confirmation request

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class,mode=EnumSource.Mode.EXCLUDE,names={"PENDING_OTP"})
    void resendConfirmationRequest_WhenReservationIsNotPendingConfirmation(ReservationStatus status){
        AppUser user = createDefaultUser();
        Car car = createCar(user.getId(),CarStatus.VERIFIED);
        Port port = createPort(PortStatus.AVAILABLE);
        Reservation reservation = createReservation(user.getId(),car.getId(),port.getId(),status);
        when(reservationRepo.findByIdAndUserId(reservation.getId(),user.getId())).thenReturn(Optional.of(reservation));

        assertThrows(ReservationConfirmationException.class,()->reservationService.resendConfirmationRequest(reservation.getId(),user.getId()));
        verifyNoInteractions(otpService);
        verifyNoInteractions(twilioService);
        verify(reservationRepo,times(1)).findByIdAndUserId(reservation.getId(),user.getId());

    }

    @Test
    void resendConfirmationRequest_WhenHappyPath(){
        AppUser appUser = createDefaultUser();
        Car car = createCar(appUser.getId(),CarStatus.VERIFIED);
        Port port = createPort(PortStatus.AVAILABLE);
        Reservation reservation = createReservation(appUser.getId(),car.getId(),port.getId(),ReservationStatus.PENDING_OTP);

        when(reservationRepo.findByIdAndUserId(reservation.getId(),appUser.getId())).thenReturn(Optional.of(reservation));
        when(otpService.generateOtpForReservation(any(), eq(appUser.getId()), eq(OtpPurpose.RESERVATION_CONFIRMATION)))
                .thenReturn("123456");

        //ACT
        reservationService.resendConfirmationRequest(reservation.getId(),appUser.getId());


        verify(otpService,times(1)).generateOtpForReservation(reservation.getId(),appUser.getId(),OtpPurpose.RESERVATION_CONFIRMATION);
        verify(twilioService,times(1)).sendSms(eq("+216"+appUser.getPhoneNumber()),
                eq("A new OTP code was generated for your reservation, please check the code and confirm your reservation :) \nCODE: 123456"));
    }

    @Test
    void resendConfirmationRequest_WhenReservationDoesNotExist(){
        AppUser appUser = createDefaultUser();

        when(reservationRepo.findByIdAndUserId(any(),eq(appUser.getId()))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class,()->reservationService.resendConfirmationRequest(UUID.randomUUID(),appUser.getId()));

        verifyNoInteractions(otpService);
        verifyNoInteractions(twilioService);
        verify(reservationRepo,times(1)).findByIdAndUserId(any(),eq(appUser.getId()));
    }



// --- Helper Methods ---an

    private Reservation createReservation(UUID userId, UUID carId,UUID portId,ReservationStatus status){
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Instant fixedStartTime = Instant.parse("2024-02-01T12:00:00Z");
        return Reservation.builder()
                .id(UUID.randomUUID())
                .reservationStatus(status)
                .carId(carId)
                .portId(portId)
                .createdAt(fixedInstant)

                .startTime(fixedStartTime)
                .endTime(fixedStartTime.plus(8,ChronoUnit.HOURS))
                .duration(Duration.EIGHT_HOURS)
                .contactNumber("12345678")
                .lateCancel(false)
                .penaltyWaived(false)
                .userId(userId)
                .build();

    }

    private ReservationConfirmationDTO createConfirmationRequest() {
        return  ReservationConfirmationDTO.builder().code("123456").build();
    }

    private void reservationAdditionStopped() {
        verifyNoInteractions(otpService);
        verifyNoInteractions(twilioService);
        verify(reservationRepo,never()).save(any(Reservation.class));
    }

    private AppUser createDefaultUser() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setPhoneNumber("12345678");
        return user;
    }

    private Port createPort(PortStatus status) {
        Port port = new Port();
        port.setId(UUID.randomUUID());
        port.setStatus(status);
        return port;
    }

    private Car createCar(UUID userId, CarStatus status) {
        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setUserId(userId); // Link car to the user or give a random UUID to simulate a car not owned by the user
        car.setStatus(status);
        return car;
    }



    private ReservationAddRequestDTO createRequestDTO(UUID carId, UUID portId) {
        ReservationAddRequestDTO dto = new ReservationAddRequestDTO();
        dto.setCarId(carId);
        dto.setPortId(portId);
        dto.setDuration(Duration.EIGHT_HOURS);
        dto.setContactNumber("12345678");
        dto.setStartDate(LocalDate.now());
        dto.setStartTimeHour(10);
        return dto;
    }

}
