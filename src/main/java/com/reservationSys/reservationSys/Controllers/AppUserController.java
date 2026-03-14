package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
public class AppUserController {

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<String> getMe(@AuthenticationPrincipal MyAppUserDetails appUser){
        return ResponseEntity.ok(appUser.getUsername());
    }



}
