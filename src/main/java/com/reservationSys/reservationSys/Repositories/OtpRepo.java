package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Models.otp.OTP;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.otp.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepo extends JpaRepository<OTP, UUID> {

    // Find the active OTP for a user (account verification)
    Optional<OTP> findByUserIdAndPurposeAndStatus(
            UUID userId,
            OtpPurpose purpose,
            OtpStatus status
    );



    // Find the active OTP for a reservation (reservation confirmation)
    Optional<OTP> findByReservationIdAndPurposeAndStatus(
            UUID reservationId,
            OtpPurpose purpose,
            OtpStatus status
    );

    // Find all pending OTPs for a reservation (to invalidate on resend)
    List<OTP> findAllByReservationIdAndStatus(
            UUID reservationId,
            OtpStatus status
    );

    // Find all pending OTPs for a user (to invalidate on resend)
    List<OTP> findAllByUserIdAndStatusAndPurpose(
            UUID userId,
            OtpStatus status,
            OtpPurpose purpose
    );

    // Find most recent OTP for a reservation (to check 2min resend cooldown)
    Optional<OTP> findTopByReservationIdOrderByCreatedAtDesc(
            UUID reservationId
    );

    // Find most recent OTP for a user (to check 2min resend cooldown)
    Optional<OTP> findTopByUserIdAndPurposeOrderByCreatedAtDesc(
            UUID userId,
            OtpPurpose purpose
    );
}
