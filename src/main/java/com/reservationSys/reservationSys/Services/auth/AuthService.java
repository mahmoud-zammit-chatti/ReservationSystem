package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.DTOs.*;
import com.reservationSys.reservationSys.Domain.otp.OTP;
import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Domain.user.RefreshToken;
import com.reservationSys.reservationSys.Domain.user.UserStatus;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.OtpRepo;
import com.reservationSys.reservationSys.Repositories.RefreshTokenRepo;
import com.reservationSys.reservationSys.Services.EmailService;
import com.reservationSys.reservationSys.Services.OTP.OtpService;
import com.reservationSys.reservationSys.Services.OTP.TwilioService;
import com.reservationSys.reservationSys.exceptions.*;
import com.twilio.exception.ApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

import static com.reservationSys.reservationSys.Domain.otp.OtpPurpose.ACCOUNT_PHONE_VERIFICATION;
import static com.reservationSys.reservationSys.Domain.otp.OtpPurpose.EMAIL_VERIFICATION;

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

    public AuthService(JwtService jwtService, AppUserRepo appUserRepo, PasswordEncoder bCryptPasswordEncoder, RefreshTokenService refreshTokenService, RefreshTokenRepo refreshTokenRepo, OtpService otpService, TwilioService twilioService, EmailService emailService, OtpRepo otpRepo) {
        this.jwtService = jwtService;
        this.appUserRepo = appUserRepo;

        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.otpService = otpService;
        this.twilioService = twilioService;
        this.emailService = emailService;
        this.otpRepo = otpRepo;
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
                .emailVerifiedAt(null)
                .phoneNumberVerifiedAt(null)
                .createdAt(Instant.now())
                .build();
        UserRegistrationResponseDTO responseDTO = UserRegistrationResponseDTO.builder()
                .email(registerUserDTO.getEmail())
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .fullName(registerUserDTO.getFullName())
                .build();
        appUserRepo.save(user);

        String code = otpService.generateOtpForUser(
                user.getId(),
                ACCOUNT_PHONE_VERIFICATION
        );
        String codeEmail = String.format("%06d", new Random().nextInt(999999));


        try {
            twilioService.sendSms(user.getPhoneNumber(), code); //only enable this when testing for the otp code sending otherwise this costs money!!!!!!!
            responseDTO.setSmsMsg("Sms was sent with success");
            responseDTO.setSmsSent(true);
        } catch (ApiException e) {
            log.warn("SMS failed for user {}: {}", user.getId(), e.getMessage());
            responseDTO.setSmsMsg("SMS failed for this registration please request another one, But your registration was a success");
            responseDTO.setSmsSent(false);
        }


        //email part
        try {
            emailService.sendVerificationEmail(user.getEmail(), codeEmail);
            responseDTO.setEmailMsg("Email was sent with success");
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
        String jwtToken = jwtService.generateToken(null, user.getEmail());

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
        String jwtToken = jwtService.generateToken(null, user.getEmail());

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
            user.setEmailVerifiedAt(Instant.now());
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
        if (otpService.verifyOtpForUser(user.getId(), code, ACCOUNT_PHONE_VERIFICATION)) {
            user.setPhoneNumberVerifiedAt(Instant.now());
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

        if (emailOtpOpt.isPresent() && emailOtpOpt.get().getCreatedAt().plusSeconds(120).isAfter(Instant.now())) {
            throw new TooManyRequestsException("Please wait before requesting another verification code for email: " + request.getEmail());
        } else {
            String code = otpService.generateOtpForUser(appUser.getId(), EMAIL_VERIFICATION);
            try {
                emailService.sendVerificationEmail(appUser.getEmail(), code);
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
        if (phoneOtpOpt.isPresent() && phoneOtpOpt.get().getCreatedAt().plusSeconds(120).isAfter(Instant.now())) {
            throw new TooManyRequestsException("Please wait before requesting another verification code for phone number: " + appUser.getPhoneNumber());
        } else {
            String code = otpService.generateOtpForUser(appUser.getId(), ACCOUNT_PHONE_VERIFICATION);
            try {
                twilioService.sendSms(appUser.getPhoneNumber(), code);
            } catch (ApiException e) {
                log.warn("SMS Resend failed for user {}: {}", appUser.getId(), e.getMessage());
                throw new SmsDeliveryException("Failed to resend verification SMS for phone number: " + appUser.getPhoneNumber());
            }
        }
    }
}

