package com.reservationSys.reservationSys.Repositories;

import java.time.Instant;
import java.util.UUID;

public interface StationWithDistanceProjection {
    UUID getStationId();
    String getName();
    String getCity();
    String getTown();
    Double getLatitude();
    Double getLongitude();
    Instant getCreatedAt();
    Double getDistanceMeters();
}
