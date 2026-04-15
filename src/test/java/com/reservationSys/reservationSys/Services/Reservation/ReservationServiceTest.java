package com.reservationSys.reservationSys.Services.Reservation;


import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationResponseDTO;
import com.reservationSys.reservationSys.Exceptions.CarExceptions.CarNotVerifiedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.NotAuthorizedException;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import com.reservationSys.reservationSys.Exceptions.PortExceptions.PortNotAvailableException;
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

    @Mock private PortRepo portRepo;
    @Mock private CarRepo carRepo;
    @Mock private ReservationRepo reservationRepo;
    @Mock private OtpService otpService;
    @Mock private TwilioService twilioService;
    @Mock private EmailService emailService;
    public final List<ReservationStatus> ACTIVESTATUS = List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN, ReservationStatus.PENDING_OTP);

    private ReservationService reservationService;
    @BeforeEach
    void setUp(){

        ZoneId realZoneId = ZoneId.of("Africa/Tunis");
        Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.systemDefault());

        reservationService = new ReservationService(reservationRepo,portRepo,carRepo, otpService, twilioService, emailService,realZoneId, fixedClock);
    }

    @Test
    void addReservation_WhenHappyPath() {
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(user.getId(), CarStatus.VERIFIED);


        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);


        Instant instant = requestDTO.getStartDate().atTime(requestDTO.getStartTimeHour(), 0).atZone(ZoneId.systemDefault()).toInstant();
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));

        when(reservationRepo.findConflictedReservations(port.getId(), instant,  instant.plus(requestDTO.getDuration().getHours(), ChronoUnit.HOURS) ,ACTIVESTATUS.stream().map(Enum::name).toList())).thenReturn(List.of());

        when(otpService.generateOtpForReservation(any(), eq(user.getId()), eq(OtpPurpose.RESERVATION_CONFIRMATION)))
                .thenReturn("123456");

        ReservationResponseDTO responseDTO = reservationService.addReservation(requestDTO, user);

        assertNotNull(responseDTO);
        assertEquals(port.getId(), responseDTO.getPortId());
        assertEquals(car.getId(), responseDTO.getCarId());
        assertEquals(ReservationStatus.PENDING_OTP, responseDTO.getStatus());


        verify(otpService,times(1)).generateOtpForReservation(any(),eq(user.getId()), eq(OtpPurpose.RESERVATION_CONFIRMATION));
        verify(twilioService,times(1)).sendSms(eq("+21612345678"), eq("123456"));
        verify(reservationRepo,times(1)).save(reservationCaptor.capture());

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(port.getId(), savedReservation.getPortId());
        assertEquals(car.getId(), savedReservation.getCarId());
        assertEquals(ReservationStatus.PENDING_OTP, savedReservation.getReservationStatus());
        assertEquals(user.getId(),savedReservation.getUserId());


    }

    @Test
    void addReservation_WhenPortIsNotAvailable(){

            AppUser user = createDefaultUser();

            Port port = createPort(PortStatus.OUT_OF_SERVICE);

            Car car = createCar(user.getId(), CarStatus.VERIFIED);

            ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        assertThrows(PortNotAvailableException.class, ()-> reservationService.addReservation(requestDTO, user));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenUserDoesNotOwnCar(){
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(UUID.randomUUID(), CarStatus.VERIFIED); // Car owned by another user

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        assertThrows(NotAuthorizedException.class, ()-> reservationService.addReservation(requestDTO, user));

        reservationAdditionStopped();

    }

    @Test
    void addReservation_WhenCarIsNotVerified(){
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(appUser.getId(), CarStatus.UNVERIFIED); // Car not verified

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));

        assertThrows(CarNotVerifiedException.class, () -> reservationService.addReservation(requestDTO, appUser));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenPortIsNotAvailableForTimeSlot(){
        AppUser user = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(user.getId(), CarStatus.VERIFIED);


        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());


        Instant instant = requestDTO.getStartDate().atTime(requestDTO.getStartTimeHour(), 0).atZone(ZoneId.systemDefault()).toInstant();
        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.of(car));


        when(reservationRepo.findConflictedReservations(port.getId(), instant,  instant.plus(requestDTO.getDuration().getHours(), ChronoUnit.HOURS) ,ACTIVESTATUS.stream().map(Enum::name).toList())).thenReturn(List.of(mock(Reservation.class)));

        assertThrows(PortNotAvailableException.class, ()-> reservationService.addReservation(requestDTO, user));

        reservationAdditionStopped();
    }

    @Test
    void addReservation_WhenCarDoesNotExist(){
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE);

        Car car = createCar(UUID.randomUUID(), CarStatus.VERIFIED); // Car that does not exist in the repo

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.of(port));
        when(carRepo.findById(car.getId())).thenReturn(Optional.empty()); // Car not found

        assertThrows(ResourceNotFound.class,()->reservationService.addReservation(requestDTO, appUser));

        reservationAdditionStopped();

    }

    @Test
    void addReservation_WhenPortDoesNotExist(){
        AppUser appUser = createDefaultUser();

        Port port = createPort(PortStatus.AVAILABLE); // Port that does not exist in the repo

        Car car = createCar(appUser.getId(), CarStatus.VERIFIED);

        ReservationAddRequestDTO requestDTO = createRequestDTO(car.getId(), port.getId());

        when(portRepo.findById(port.getId())).thenReturn(Optional.empty()); // Port not found

        assertThrows(ResourceNotFound.class,()->reservationService.addReservation(requestDTO, appUser));

        reservationAdditionStopped();

    }




// --- Helper Methods ---

    private void reservationAdditionStopped() {
        verifyNoInteractions(otpService);
        verifyNoInteractions(twilioService);
        verifyNoInteractions(reservationRepo);
    }

    private AppUser createDefaultUser() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
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
