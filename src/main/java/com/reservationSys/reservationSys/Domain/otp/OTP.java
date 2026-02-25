package com.reservationSys.reservationSys.Domain.otp;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private UUID reservationId;

    private String code;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    private OtpStatus status=OtpStatus.PENDING;




}
