package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.user.AppUser;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepo extends JpaRepository<AppUser,UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AppUser> findById(@NonNull UUID id);
}
