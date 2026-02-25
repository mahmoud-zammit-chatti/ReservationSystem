package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.Domain.user.AppUser;
import com.reservationSys.reservationSys.Domain.user.RefreshToken;
import com.reservationSys.reservationSys.Repositories.AppUserRepo;
import com.reservationSys.reservationSys.Repositories.RefreshTokenRepo;
import com.reservationSys.reservationSys.exceptions.AuthenticationError;
import com.reservationSys.reservationSys.exceptions.RessourceNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;
    private final AppUserRepo appUserRepo;

    @Value("#{${REFRESH_TOKEN_EXPIRATION_DAYS} * 24 * 60 * 60000}")
    private Long refreshTokenExpiration;
    @Value("${REFRESH_TOKEN_SECRET}")
    private String refreshTokenSecret;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, AppUserRepo appUserRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.appUserRepo = appUserRepo;
    }


    public RefreshToken generateRefreshToken(AppUser user){

        RefreshToken generated = new RefreshToken();
        generated.setToken(UUID.randomUUID().toString());
        generated.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        generated.setIsRevoked(false);
        generated.setId(user.getId().toString());
        generated.setCreatedAt(Instant.now());
        refreshTokenRepo.save(generated);
        return generated;
    }

    public RefreshToken validateRefreshToken(String token){

        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(()-> new RessourceNotFound("Refresh token not found"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())){
            refreshToken.setIsRevoked(true);
            refreshTokenRepo.save(refreshToken);
            throw new AuthenticationError("Refresh token expired");
        }
        if(refreshToken.getIsRevoked()){
            throw new AuthenticationError("Refresh token revoked");
        }
        return refreshToken;
    }

    public RefreshToken rotateRefreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(()-> new RessourceNotFound("Refresh token not found"));
        if (refreshToken.getExpiresAt().isBefore(Instant.now())){
            refreshToken.setIsRevoked(true);
            refreshTokenRepo.save(refreshToken);
            throw new AuthenticationError("Refresh token expired");
        }
        if(refreshToken.getIsRevoked()){
            throw new AuthenticationError("Refresh token revoked");
        }
        String userId = refreshToken.getUserId();
        AppUser user = appUserRepo.findById(Long.parseLong(userId)).orElseThrow(()-> new RessourceNotFound("User not found"));
        refreshToken.setIsRevoked(true);
        return generateRefreshToken(user);
    }





}
