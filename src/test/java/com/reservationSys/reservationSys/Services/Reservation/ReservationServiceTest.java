package com.reservationSys.reservationSys.Services.Reservation;

import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationAddRequestDTO;
import com.reservationSys.reservationSys.DTOs.ReservationDTOs.ReservationResponseDTO;
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
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
        ZoneId realZoneId = ZoneId.systemDefault();
        Clock realClock = Clock.system(realZoneId);

        reservationService = new ReservationService(reservationRepo,portRepo,carRepo, otpService, twilioService, emailService,realZoneId, realClock);
    }






    @Test
    void addReservation_WhenHappyPath() {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());

        Port port = new Port();
        port.setId(UUID.randomUUID());
        port.setStatus(PortStatus.AVAILABLE);

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setUserId(user.getId());
        car.setStatus(CarStatus.VERIFIED);

        ReservationAddRequestDTO requestDTO = new ReservationAddRequestDTO();
        requestDTO.setCarId(car.getId());
        requestDTO.setPortId(port.getId());
        requestDTO.setDuration(Duration.EIGHT_HOURS);
        requestDTO.setContactNumber("12345678");
        requestDTO.setStartDate(LocalDate.now());
        requestDTO.setStartTimeHour(10);

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
        verify(reservationRepo,times(1)).save(any(Reservation.class));


    }


}
