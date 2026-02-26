package com.reservationSys.reservationSys.Services.OTP;


import com.reservationSys.reservationSys.Domain.otp.OTP;
import com.reservationSys.reservationSys.Domain.otp.OtpPurpose;
import com.reservationSys.reservationSys.Domain.otp.OtpStatus;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Service
public class OtpService {

    private final OtpRepo otpRepo;

    public OtpService(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }

    public String generateOtpForUser(UUID userId, OtpPurpose purpose){

//        List<OTP> oldOTPs= otpRepo.findAllByUserIdAndStatus(userId, OtpStatus.PENDING);
//
//        oldOTPs.forEach(otp -> {
//            otp.setStatus(OtpStatus.INVALIDATED);
//
//        });
//        otpRepo.saveAll(oldOTPs);

        OTP otp = OTP.builder()
                .code(String.format("%06d", new Random().nextInt(999999)))
                .purpose(purpose)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60*60))
                .status(OtpStatus.PENDING)
                .userId(userId)
                .reservationId(null)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);


        return otp.getCode();
    }
    public String generateOtpForReservation(UUID reservationId, OtpPurpose purpose){

        List<OTP> oldOTPs= otpRepo.findAllByReservationIdAndStatus(reservationId, OtpStatus.PENDING);

        oldOTPs.forEach(otp -> {
            otp.setStatus(OtpStatus.INVALIDATED);

        });
        otpRepo.saveAll(oldOTPs);

        OTP otp = OTP.builder()
                .code(String.format("%06d", new Random().nextInt(999999)))
                .purpose(purpose)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60*60))
                .status(OtpStatus.PENDING)
                .userId(null)
                .reservationId(reservationId)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);
        return otp.getCode();
    }


}
