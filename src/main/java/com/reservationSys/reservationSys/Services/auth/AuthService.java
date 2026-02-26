package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.DTOs.AuthResponseDTO;
import com.reservationSys.reservationSys.DTOs.LoginUserDTO;
import com.reservationSys.reservationSys.DTOs.RegisterUserDTO;
import com.reservationSys.reservationSys.DTOs.UserRegistrationResponseDTO;
import com.reservationSys.reservationSys.Domain.otp.OtpPurpose;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Domain.user.RefreshToken;
import com.reservationSys.reservationSys.Domain.user.UserStatus;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.RefreshTokenRepo;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.exceptions.IncorrectCredentials;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import com.reservationSys.reservationSys.exceptions.UserAlreadyExists;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final AppUserRepo appUserRepo;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final OtpService otpService;
    private final TwilioService twilioService;

    public AuthService(JwtService jwtService, AppUserRepo appUserRepo, PasswordEncoder bCryptPasswordEncoder, RefreshTokenService refreshTokenService, RefreshTokenRepo refreshTokenRepo, OtpService otpService, TwilioService twilioService) {
        this.jwtService = jwtService;
        this.appUserRepo = appUserRepo;

        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.otpService = otpService;
        this.twilioService = twilioService;
    }

    @Transactional
    public UserRegistrationResponseDTO register(RegisterUserDTO registerUserDTO) {
        if(appUserRepo.existsByEmail(registerUserDTO.getEmail())){
            throw new UserAlreadyExists("User with email: "+registerUserDTO.getEmail()+" already exists");

        }
        String hash = bCryptPasswordEncoder.encode(registerUserDTO.getPassword());
        AppUser user = AppUser.builder()
                .fullName(registerUserDTO.getFullName())
                .email(registerUserDTO.getEmail())
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .passwordHash(hash)
                .status(UserStatus.INACTIVE)
                .emailVerifiedAt(null)
                .phoneNumberVerifiedAt(null)
                .createdAt(Instant.now())
                .build();
        UserRegistrationResponseDTO responseDTO = UserRegistrationResponseDTO.builder()
                .email( registerUserDTO.getEmail())
                .phoneNumber( registerUserDTO.getPhoneNumber())
                .fullName( registerUserDTO.getFullName())
                .build();
        appUserRepo.save(user);

        String code = otpService.generateOtpForUser(
                user.getId(),
                OtpPurpose.ACCOUNT_VERIFICATION
        );
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        twilioService.sendSms(user.getPhoneNumber(), code);
                    }
                }
        );

        return responseDTO;
    }

    @Transactional
    public AuthResponseDTO login(LoginUserDTO user){
        AppUser appUser = appUserRepo.findByEmail(user.getEmail()).orElseThrow(()-> new IncorrectCredentials("User not found"));
        if(!bCryptPasswordEncoder.matches(user.getPassword(),appUser.getPasswordHash())){
            throw new IncorrectCredentials("Incorrect Password for user: "+user.getEmail());
        }
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(appUser);
        String jwtToken= jwtService.generateToken(null,user.getEmail());

        return AuthResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public String logout(String token){
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RessourceNotFound("Refresh token not found"));
        refreshToken.setIsRevoked(true);
        refreshTokenRepo.save(refreshToken);
        return "Logged out successfully";
    }

    @Transactional
    public AuthResponseDTO refresh(String token){
        // rotateRefreshToken already validates, no need to call validateRefreshToken separately
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(token);
        AppUser user = appUserRepo.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new RessourceNotFound("User not found"));
        String jwtToken = jwtService.generateToken(null, user.getEmail());

        return AuthResponseDTO.builder()
                .refreshToken(newRefreshToken.getToken())
                .accessToken(jwtToken)
                .build();
    }
}
