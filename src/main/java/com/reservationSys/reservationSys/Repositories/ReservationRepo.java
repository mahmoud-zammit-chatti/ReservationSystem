package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.reservation.Reservation;
import com.reservationSys.reservationSys.Domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, UUID> {

    List<Reservation> findAllByPortIdAndReservationStatusIn(UUID portId, List<ReservationStatus> reservationStatusList);
}
