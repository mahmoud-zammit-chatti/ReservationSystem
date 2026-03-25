package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.port.Port;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortRepo extends JpaRepository<Port, UUID> {

    List<Port> findAllByStation_StationId(UUID stationId);
}
