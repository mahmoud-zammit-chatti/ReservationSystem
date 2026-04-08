package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.AuthDTOs.*;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Services.auth.AuthService;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDTO> register ( @Valid @RequestBody RegisterUserDTO registerUserDTO ){

        return ResponseEntity.ok( authService.register(registerUserDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginUserDTO loginUserDTO){
        return ResponseEntity.ok().body(authService.login(loginUserDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequestDTO request){
        return ResponseEntity.ok(authService.logout(request.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request){
        return ResponseEntity.ok().body(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody EmailVerificationDTO request){
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@Valid @RequestBody EmailVerificationRequestDTO request){
        authService.resendEmailVerification(request);
        return ResponseEntity.ok("Verification email resent successfully");
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(@AuthenticationPrincipal MyAppUserDetails user, @Valid @RequestBody PhoneVerificationDTO request){
        AppUser appUser = user.getAppUser();
        authService.verifyPhoneNumber(appUser,request.getCode());
        return ResponseEntity.ok("Phone verified successfully");
    }

    @PostMapping("/resend-verification-phone")
    @SecurityRequirement(name = "Bearer Authentication")

    public ResponseEntity<String> resendVerificationPhone(@AuthenticationPrincipal MyAppUserDetails user){
        AppUser appUser = user.getAppUser();
        authService.resendPhoneVerification(appUser);
        return ResponseEntity.ok("Verification phone resent successfully");
    }


}
