package com.reservationSys.reservationSys.security;

import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Models.user.UserStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MyAppUserDetails implements UserDetails {

    private final AppUser appUser;

    public MyAppUserDetails(AppUser appUser) {
        this.appUser = appUser;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getUserRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return appUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return appUser.getStatus() == UserStatus.ACTIVE;
    }
}


