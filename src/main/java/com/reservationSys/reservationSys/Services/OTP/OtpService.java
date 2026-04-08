package com.reservationSys.reservationSys.Services.OTP;


import com.reservationSys.reservationSys.Models.otp.OTP;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.otp.OtpStatus;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;


@Service
public class OtpService {

    private final OtpRepo otpRepo;

    private final ZoneId businessZoneId;
    private final Clock buisnessClock;

    public OtpService(OtpRepo otpRepo, ZoneId businessZoneId, Clock buisnessClock) {
        this.otpRepo = otpRepo;
        this.businessZoneId = businessZoneId;
        this.buisnessClock = buisnessClock;
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
                .createdAt(Instant.now(buisnessClock))
                .expiresAt(Instant.now(buisnessClock).plusSeconds(60*60))
                .status(OtpStatus.PENDING)
                .userId(userId)
                .reservationId(null)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);


        return otp.getCode();
    }
    public String generateOtpForReservation(UUID reservationId,UUID userId, OtpPurpose purpose){

        List<OTP> oldOTPs= otpRepo.findAllByReservationIdAndStatus(reservationId, OtpStatus.PENDING);

        oldOTPs.forEach(otp -> {
            otp.setStatus(OtpStatus.INVALIDATED);

        });
        otpRepo.saveAll(oldOTPs);

        OTP otp = OTP.builder()
                .code(String.format("%06d", new Random().nextInt(999999)))
                .purpose(purpose)
                .createdAt(Instant.now(buisnessClock))
                .expiresAt(Instant.now(buisnessClock).plusSeconds(60*60))
                .status(OtpStatus.PENDING)
                .userId(userId)
                .reservationId(reservationId)
                .verifiedAt(null)
                .build();
        otpRepo.save(otp);
        return otp.getCode();
    }

    public boolean verifyOtp(UUID ID, String code, OtpPurpose purpose){
        OTP otp;
        if(purpose==OtpPurpose.RESERVATION_CONFIRMATION){
             otp = otpRepo.findByReservationIdAndPurposeAndStatus(ID, purpose, OtpStatus.PENDING)
                    .orElseThrow(() -> new RuntimeException("No pending OTP found for reservation with id: " + ID));
        }else {
             otp = otpRepo.findByUserIdAndPurposeAndStatus(ID, purpose, OtpStatus.PENDING)
                    .orElseThrow(() -> new RuntimeException("No pending OTP found for user with id: " + ID));
        }
        if(otp.getExpiresAt().isBefore(Instant.now(buisnessClock))){
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepo.save(otp);
            throw new RuntimeException("OTP has expired, Please request for another OTP code");
        }
        System.out.println(otp.getCode() + " " + code);
        if(otp.getCode().equals(code)){
            otp.setStatus(OtpStatus.VERIFIED);
            otp.setVerifiedAt(Instant.now(buisnessClock));
            otpRepo.save(otp);
            return true;
        } else {
            return false;
        }
    }



}
