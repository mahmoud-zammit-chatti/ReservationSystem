package com.reservationSys.reservationSys.Domain.user;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String userId;

    private Boolean isRevoked;

    private Instant createdAt;

    private Instant expiresAt;

}
