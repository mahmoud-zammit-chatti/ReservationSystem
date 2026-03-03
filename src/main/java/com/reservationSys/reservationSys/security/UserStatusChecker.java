package com.reservationSys.reservationSys.security;



import com.reservationSys.reservationSys.Domain.user.UserStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserStatusChecker {

    public boolean isActive(Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()){
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof MyAppUserDetails)) {
            return false;
        }
        MyAppUserDetails appUser = (MyAppUserDetails) principal;
        return  appUser.getAppUser().getStatus()== UserStatus.ACTIVE;
    }
    public boolean isEmailVerified(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof MyAppUserDetails)) return false;
        MyAppUserDetails userDetails = (MyAppUserDetails) principal;
        return userDetails.getAppUser().getEmailVerifiedAt() != null;
    }

}
