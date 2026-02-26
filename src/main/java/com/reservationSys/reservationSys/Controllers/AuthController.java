package com.reservationSys.reservationSys.Controllers;


import com.reservationSys.reservationSys.DTOs.AuthResponseDTO;
import com.reservationSys.reservationSys.DTOs.LoginUserDTO;
import com.reservationSys.reservationSys.DTOs.RefreshTokenRequestDTO;
import com.reservationSys.reservationSys.DTOs.RegisterUserDTO;
import com.reservationSys.reservationSys.DTOs.UserRegistrationResponseDTO;
import com.reservationSys.reservationSys.Services.auth.AuthService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserRegistrationResponseDTO> register(@RequestBody RegisterUserDTO registerUserDTO ){

        return ResponseEntity.ok( authService.register(registerUserDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginUserDTO loginUserDTO){
        return ResponseEntity.ok().body(authService.login(loginUserDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequestDTO request){
        return ResponseEntity.ok(authService.logout(request.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO request){
        return ResponseEntity.ok().body(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(){
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(){
        return ResponseEntity.ok("Verification email resent successfully");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(){
        return ResponseEntity.ok("Phone verified successfully");
    }
    @PostMapping("/resend-verification-phone")
    public ResponseEntity<String> resendVerificationPhone(){
        return ResponseEntity.ok("Verification phone resent successfully");
    }


}
