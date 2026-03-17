package com.reservationSys.reservationSys.Services.OTP;


import com.reservationSys.reservationSys.Domain.otp.OTP;
import com.reservationSys.reservationSys.Domain.otp.OtpPurpose;
import com.reservationSys.reservationSys.Domain.otp.OtpStatus;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public String generateOtpForUser(UUID userId, OtpPurpose purpose){

        List<OTP> oldOTPs= otpRepo.findAllByUserIdAndStatusAndPurpose(userId, OtpStatus.PENDING, purpose);

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
                .userId(userId)
                .reservationId(null)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);


        return otp.getCode();
    }
    public String generateOtpForReservation(UUID reservationId, OtpPurpose purpose){

//        List<OTP> oldOTPs= otpRepo.findAllByReservationIdAndStatus(reservationId, OtpStatus.PENDING);
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
                .userId(null)
                .reservationId(reservationId)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);
        return otp.getCode();
    }

    public boolean verifyOtpForUser(UUID userId, String code, OtpPurpose purpose){
        OTP otp = otpRepo.findByUserIdAndPurposeAndStatus(userId, purpose, OtpStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No pending OTP found for user with id: " + userId));

        if(otp.getExpiresAt().isBefore(Instant.now())){
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepo.save(otp);
            throw new RuntimeException("OTP has expired, Please request for another OTP code");
        }
        System.out.println(otp.getCode() + " " + code);
        if(otp.getCode().equals(code)){
            otp.setStatus(OtpStatus.VERIFIED);
            otp.setVerifiedAt(Instant.now());
            otpRepo.save(otp);
            return true;
        } else {
            return false;
        }
    }



}
