package com.reservationSys.reservationSys.Domain.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private String fullName;

    @Column(unique = false, nullable = false, length = 12) //unique should be true , keep false just for testing
    private String phoneNumber;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Instant emailVerifiedAt;

    private Instant phoneNumberVerifiedAt;

    private Instant createdAt;


}
