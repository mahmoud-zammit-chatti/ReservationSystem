package com.reservationSys.reservationSys.security;

import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class MyAppUserDetailsService implements UserDetailsService {

    private final AppUserRepo appUserRepo;

    public MyAppUserDetailsService(AppUserRepo appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @Override
    public MyAppUserDetails loadUserByUsername(@NonNull String email) {
        AppUser appUser = appUserRepo.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFound("If this email exists, a verification code has been sent")
        );
        return new MyAppUserDetails(appUser);
    }

    public AppUser findByEmail(String email){
        return appUserRepo.findByEmail(email).orElseThrow(()-> new ResourceNotFound("If this email exists, a verification code has been sent"));
    }



}
