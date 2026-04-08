package com.reservationSys.reservationSys.Models.port;

import com.reservationSys.reservationSys.Models.station.Station;
import jakarta.persistence.*;
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
public class Port{

    private static final int ACCESS_ID_LENGTH = 6;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();



    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    private String name;

    @Column(name = "access_identifier",nullable = false,unique = true,length = ACCESS_ID_LENGTH,updatable = false)
    private String accessIdentifier;

    @Enumerated(EnumType.STRING)
    private PortStatus status;

    private Instant createdAt;

    @PrePersist
    private void ensureAccessIdentifier() {
        if (this.accessIdentifier == null || this.accessIdentifier.isBlank()) {
            this.accessIdentifier = randomCode();
        }
    }

    private static String randomCode() {
        StringBuilder sb = new StringBuilder(Port.ACCESS_ID_LENGTH);
        for (int i = 0; i < Port.ACCESS_ID_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
