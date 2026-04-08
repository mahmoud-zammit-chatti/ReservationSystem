package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Models.reservation.Reservation;
import com.reservationSys.reservationSys.Models.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, UUID> {

    List<Reservation> findAllByPortIdAndReservationStatusIn(UUID portId, List<ReservationStatus> reservationStatusList);

    @Query(value = """
    SELECT r.*
    FROM reservation r
    JOIN port p ON p.id = r.port_id
    WHERE r.port_id = :portId
      AND p.station_id = :stationId
      AND r.reservation_status IN (:statuses)
      AND r.end_time > :dayStart
      AND r.start_time < :dayEnd
    """, nativeQuery = true)
    List<Reservation> findByPortStationAndDate(
            @Param("portId") UUID portId,
            @Param("stationId") UUID stationId,
            @Param("statuses") List<String> statuses,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );


    @Query(value = """
SELECT r.*
FROM reservation r
WHERE r.port_id = :portId
AND r.reservation_status IN (:activeStatus)
AND r.start_time<:endTime
AND r.end_time>:startTime
""",nativeQuery = true)
    List<Reservation> findConflictedReservations(@Param("portId") UUID id, @Param("startTime") Instant candidateStartTime,@Param("endTime") Instant candidateEndTime,@Param("activeStatus") List<String> activestatus);

    List<Reservation> findAllByUserId(UUID id);

    List<Reservation> findAllByUserIdAndReservationStatusEquals(UUID id, ReservationStatus status);

    List<Reservation> findAllByUserIdAndCarId(UUID id, UUID carId);

    Optional<Reservation> findByIdAndUserId(UUID reservationId, UUID id);

    List<Reservation> findAllByCarId(UUID id);
}
