package com.reservationSys.reservationSys.Domain.otp;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OTP {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    private UUID reservationId; // reservation verification so i can get the phone number use for the reservation

    private UUID userId; //account verification

    private String code;

    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant verifiedAt;

    @Enumerated(EnumType.STRING)
    private OtpStatus status=OtpStatus.PENDING;




}
