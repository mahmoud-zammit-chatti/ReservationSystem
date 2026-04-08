package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Models.station.Station;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StationRepo extends JpaRepository<Station, UUID> {


    Optional<Station> findByStationId(@NonNull UUID id);


    Optional<Station> deleteByStationId(UUID id);

    @Query(value = """
SELECT s.station_id AS stationId,
    s.name AS name,
    s.city AS city,
    s.town AS town,
    s.latitude AS latitude,
    s.longitude AS longitude,
    s.created_at AS createdAt
       ,earth_distance(ll_to_earth(s.latitude,s.longitude ), ll_to_earth(:userLat, :userLng)) as distanceMeters
FROM station s
WHERE earth_distance(
        ll_to_earth(s.latitude,s.longitude),
        ll_to_earth(:userLat, :userLng)
    ) <= :radius
    ORDER BY distanceMeters
""",nativeQuery = true)
    List<StationWithDistanceProjection> findStationWithinRadius(
            @Param("radius") double radius, @Param("userLng") double userLongitude,@Param("userLat") double userLatitude);
}
