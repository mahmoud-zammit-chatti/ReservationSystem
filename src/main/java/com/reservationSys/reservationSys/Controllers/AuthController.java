package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.AuthDTOs.*;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.Services.auth.AuthService;
import com.reservationSys.reservationSys.Security.MyAppUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.reservationSys.reservationSys.Models.otp.OtpPurpose.ACCOUNT_PHONE_VERIFICATION;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AppUserRepo appUserRepo;
    private final OtpService otpService;
    private final TwilioService twilioService;
    public AuthController(AuthService authService, AppUserRepo appUserRepo, OtpService otpService, TwilioService twilioService) {
        this.authService = authService;
        this.appUserRepo = appUserRepo;
        this.otpService = otpService;
        this.twilioService = twilioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDTO> register ( @Valid @RequestBody RegisterUserDTO registerUserDTO ){

        return ResponseEntity.ok( authService.register(registerUserDTO));
    }
/*
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
*/
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

    @PostMapping("/verify-phone-mock")
    public ResponseEntity<String> verifyPhone(@Valid @RequestBody PhoneVerificationDTO request){

        AppUser user = appUserRepo.findByEmail("mahmoudzammit18@gmail.com").orElseThrow(() -> new RuntimeException("User not found"));



        authService.verifyPhoneNumber(user,request.getCode());
        return ResponseEntity.ok("Phone verified successfully");
    }

    @PostMapping("/resend-verification-phone-mock")
    public ResponseEntity<String> resendVerificationPhone(){
        AppUser appUser = appUserRepo.findByEmail("mahmoudzammit18@gmail.com").orElseThrow(() -> new RuntimeException("User not found"));
        authService.resendPhoneVerification(appUser);

        return ResponseEntity.ok("Verification phone resent successfully");
    }
    @GetMapping("rahouma")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("3aslemaaa rahoumaa l katkoutaaa, n7ebek barchaaaa");
    }


}
