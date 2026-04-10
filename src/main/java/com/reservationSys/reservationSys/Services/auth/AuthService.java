package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.DTOs.AuthDTOs.*;
import com.reservationSys.reservationSys.Models.otp.OTP;
import com.reservationSys.reservationSys.Models.otp.OtpPurpose;
import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Models.user.RefreshToken;
import com.reservationSys.reservationSys.Models.user.UserRole;
import com.reservationSys.reservationSys.Models.user.UserStatus;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import com.reservationSys.reservationSys.Repositories.RefreshTokenRepo;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.Exceptions.AuthExceptions.*;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.RessourceNotFound;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.TooManyRequestsException;
import com.twilio.exception.ApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static com.reservationSys.reservationSys.Models.otp.OtpPurpose.ACCOUNT_PHONE_VERIFICATION;
import static com.reservationSys.reservationSys.Models.otp.OtpPurpose.EMAIL_VERIFICATION;

@Slf4j
@Service
public class AuthService {
    private final JwtService jwtService;
    private final AppUserRepo appUserRepo;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final OtpService otpService;
    private final TwilioService twilioService;
    private final EmailService emailService;
    private final OtpRepo otpRepo;


    private final ZoneId buisnesZoneId;
    private final Clock buisnesClock;

    public AuthService(JwtService jwtService, AppUserRepo appUserRepo, PasswordEncoder bCryptPasswordEncoder, RefreshTokenService refreshTokenService, RefreshTokenRepo refreshTokenRepo, OtpService otpService, TwilioService twilioService, EmailService emailService, OtpRepo otpRepo, ZoneId buisnesZoneId, Clock buisnesClock) {
        this.jwtService = jwtService;
        this.appUserRepo = appUserRepo;

        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.otpService = otpService;
        this.twilioService = twilioService;
        this.emailService = emailService;
        this.otpRepo = otpRepo;
        this.buisnesZoneId = buisnesZoneId;
        this.buisnesClock = buisnesClock;
    }

    @Transactional
    public UserRegistrationResponseDTO register(RegisterUserDTO registerUserDTO) {
        if (appUserRepo.existsByEmail(registerUserDTO.getEmail())) {
            throw new UserAlreadyExists("User with email: " + registerUserDTO.getEmail() + " already exists");

        }
        String hash = bCryptPasswordEncoder.encode(registerUserDTO.getPassword());
        AppUser user = AppUser.builder()
                .fullName(registerUserDTO.getFullName())
                .email(registerUserDTO.getEmail())
                .phoneNumber("+216" + registerUserDTO.getPhoneNumber())//adding the ability to choose country code in the future
                .passwordHash(hash)
                .status(UserStatus.INACTIVE)
                .userRole(UserRole.USER)
                .emailVerifiedAt(null)
                .phoneNumberVerifiedAt(Instant.now(buisnesClock))
                .createdAt(Instant.now(buisnesClock))
                .build();
        UserRegistrationResponseDTO responseDTO = UserRegistrationResponseDTO.builder()
                .email(registerUserDTO.getEmail())
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .fullName(registerUserDTO.getFullName())
                .build();
        appUserRepo.save(user);

        String codeSms = otpService.generateOtpForUser(
                user.getId(),
                ACCOUNT_PHONE_VERIFICATION
        );
        String codeEmail = otpService.generateOtpForUser(
                user.getId(),
                EMAIL_VERIFICATION
        );


        try {
           // twilioService.sendSms(user.getPhoneNumber(), codeSms); //only enable this when testing for the otp code sending otherwise this costs money!!!!!!!
            responseDTO.setSmsMsg("due to lack of credit in the api the sms sending feature is currently paused, feel free to check the code to see the implementation in authService.java line 96");
            responseDTO.setSmsSent(true);
        } catch (ApiException e) {
            log.warn("SMS failed for user {}: {}", user.getId(), e.getMessage());
            responseDTO.setSmsMsg("SMS failed for this registration please request another one, But your registration was a success");
            responseDTO.setSmsSent(false);
        }


        //email part
        try {
            emailService.sendVerificationEmail(user.getEmail(), codeEmail,"Email verification for E-Car reservation system");
            responseDTO.setEmailMsg("Email was sent with success, please verify with the code you just received");
            responseDTO.setEmailSent(true);
        } catch (MailException e) {
            log.warn("Email failed for user {}: {}", user.getId(), e.getMessage());
            responseDTO.setEmailMsg("Email failed for this registration please request another one, But your registration was a success");
            responseDTO.setEmailSent(false);
        }


        return responseDTO;
    }

    @Transactional
    public AuthResponseDTO login(LoginUserDTO user) {

        AppUser appUser = appUserRepo.findByEmail(user.getEmail()).orElseThrow(() -> new IncorrectCredentials("User not found"));

        if (appUser.getEmailVerifiedAt() == null) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        if (!bCryptPasswordEncoder.matches(user.getPassword(), appUser.getPasswordHash())) {
            throw new IncorrectCredentials("Incorrect Password for user: " + user.getEmail());
        }
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(appUser);
        String jwtToken = jwtService.generateToken(
                Map.of("role", appUser.getUserRole().name()),
                appUser.getEmail()
        );

        return AuthResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public String logout(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RessourceNotFound("Refresh token not found"));
        refreshToken.setIsRevoked(true);
        refreshTokenRepo.save(refreshToken);
        return "Logged out successfully";
    }

    @Transactional
    public AuthResponseDTO refresh(String token) {
        // rotateRefreshToken already validates, no need to call validateRefreshToken separately
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(token);
        AppUser user = appUserRepo.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new RessourceNotFound("User not found"));
        String jwtToken = jwtService.generateToken(
                Map.of("role", user.getUserRole().name()),
                user.getEmail()
        );

        return AuthResponseDTO.builder()
                .refreshToken(newRefreshToken.getToken())
                .accessToken(jwtToken)
                .build();
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        AppUser user = appUserRepo.findByEmail(email).orElseThrow(() -> new RessourceNotFound("If this email exists, a verification code has been sent"));
        if (user.getEmailVerifiedAt() != null) {
            throw new IncorrectCredentials("Email already verified ");
        }
        if (twilioService.verifyEmailCode(user.getId(), code)) {
            user.setEmailVerifiedAt(Instant.now(buisnesClock));
            appUserRepo.save(user);
        } else {
            throw new IncorrectCredentials("Invalid verification code for email: " + email);
        }
    }

    @Transactional
    public void verifyPhoneNumber(AppUser user, String code) {
        String phoneNumber = user.getPhoneNumber();
        if (user.getPhoneNumberVerifiedAt() != null) {
            throw new IncorrectCredentials("Phone number already verified ");
        }
        if (otpService.verifyOtp(user.getId(), code, ACCOUNT_PHONE_VERIFICATION)) {
            user.setPhoneNumberVerifiedAt(Instant.now(buisnesClock));
            user.setStatus(UserStatus.ACTIVE);
            appUserRepo.save(user);
        } else {
            throw new IncorrectCredentials("Invalid verification code for phone number: " + phoneNumber);
        }
    }

    public void resendEmailVerification(@Valid EmailVerificationRequestDTO request) {
        Optional<AppUser> appUserOpt = appUserRepo.findByEmail(request.getEmail());
        if (appUserOpt.isEmpty()) {
            return; // to prevent email enumeration attacks, we return a success response even if the email doesn't exist in the database, but we don't send an email in this case
        }
        AppUser appUser = appUserOpt.get();
        if (appUser.getEmailVerifiedAt() != null) {
            throw new EmailAleadyVerifed("Email already verified for user with email: " + request.getEmail());
        }
        Optional<OTP> emailOtpOpt = otpRepo.findTopByUserIdAndPurposeOrderByCreatedAtDesc(appUser.getId(), EMAIL_VERIFICATION);

        if (emailOtpOpt.isPresent() && emailOtpOpt.get().getCreatedAt().plusSeconds(120).isAfter(Instant.now(buisnesClock))) {
            throw new TooManyRequestsException("Please wait before requesting another verification code for email: " + request.getEmail());
        } else {
            String code = otpService.generateOtpForUser(appUser.getId(), EMAIL_VERIFICATION);
            try {
                emailService.sendVerificationEmail(appUser.getEmail(), code,"Email verification for E-Car reservation system");
            } catch (MailException e) {
                log.warn("Email Resend failed for user {}: {}", appUser.getId(), e.getMessage());
                throw new EmailDeliveryException("Failed to resend verification email for email: " + request.getEmail());
            }

        }
    }


    public void resendPhoneVerification(AppUser appUser) {
        if (appUser.getPhoneNumberVerifiedAt() != null) {
            throw new PhoneNumberAlreadyVerified("Phone number already verified for user with email: " + appUser.getEmail());
        }
        Optional<OTP> phoneOtpOpt = otpRepo.findTopByUserIdAndPurposeOrderByCreatedAtDesc(appUser.getId(), ACCOUNT_PHONE_VERIFICATION);
        if (phoneOtpOpt.isPresent() && phoneOtpOpt.get().getCreatedAt().plusSeconds(120).isAfter(Instant.now(buisnesClock))) {
            throw new TooManyRequestsException("Please wait before requesting another verification code for phone number: " + appUser.getPhoneNumber());
        } else {
            String code = otpService.generateOtpForUser(appUser.getId(), OtpPurpose.ACCOUNT_PHONE_VERIFICATION);
            try {
                twilioService.sendSms(appUser.getPhoneNumber(), code);
            } catch (ApiException e) {
                log.warn("SMS Resend failed for user {}: {}", appUser.getId(), e.getMessage());
                throw new SmsDeliveryException("Failed to resend verification SMS for phone number: " + appUser.getPhoneNumber());
            }
        }
    }
}

