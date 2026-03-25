package com.reservationSys.reservationSys.Domain.port;

import com.reservationSys.reservationSys.Domain.station.Station;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Port{
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    private String name;

    private String accessIdentifier;

    @Enumerated(EnumType.STRING)
    private PortStatus status;
}
