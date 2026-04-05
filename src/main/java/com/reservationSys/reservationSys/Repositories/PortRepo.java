package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.port.Port;
import com.reservationSys.reservationSys.Domain.port.PortStatus;
import com.reservationSys.reservationSys.Domain.reservation.Duration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortRepo extends JpaRepository<Port, UUID> {

    List<Port> findAllByStation_StationId(UUID stationId);

    @Query(value = """
SELECT p.* FROM Port p
where p.status='AVAILABLE' AND p.station_id = :stationId AND NOT EXISTS(
    SELECT 1 FROM reservation r
    WHERE r.port_id = p.id
    AND r.start_time< :endTime
    AND  r.end_time > :startTime
)
""",nativeQuery = true)
    List<Port> findAllByStation_StationIdAndIsAvailableTrue(
            @Param("stationId") UUID stationId,
            @Param("startTime")Instant startTime,
            @Param("endTime") Instant endTime);

    Optional<Port> findByIdAndStation_StationId(UUID portId,UUID stationId);

    Optional<Port> deleteByIdAndStation_StationId(UUID portId, UUID stationId);

    @Query(value = """
            SELECT p.*
            from port p
            WHERE p.status='EXPIRING_SOON'
            AND NOT EXISTS(
                SELECT 1 FROM reservation r
                         WHERE r.port_id = p.id
                         AND( r.reservation_status= 'CONFIRMED'
                         OR r.reservation_status='PENDING_OTP'
                         OR r.reservation_status='CHECKED_IN'
                         )
            );
            """
            ,nativeQuery = true)
    List<Port> PortsToBeDeleted();
}
