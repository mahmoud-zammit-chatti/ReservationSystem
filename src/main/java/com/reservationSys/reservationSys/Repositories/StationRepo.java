package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.station.Station;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StationRepo extends JpaRepository<Station, UUID> {


    Optional<Station> findByStationId(@NonNull UUID id);


    Optional<Station> deleteByStationId(UUID id);

}
