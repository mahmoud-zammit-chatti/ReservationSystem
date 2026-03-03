package com.reservationSys.reservationSys.Repositories;

import com.reservationSys.reservationSys.Domain.user.AppUser;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser,UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AppUser> findById(@NonNull UUID id);

    Optional<AppUser> findByPhoneNumber(String phoneNumber);
}
