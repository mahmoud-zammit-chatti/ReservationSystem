package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.Models.user.AppUser;
import com.reservationSys.reservationSys.Models.user.RefreshToken;
import com.reservationSys.reservationSys.Repositories.RefreshTokenRepo;
import com.reservationSys.reservationSys.Exceptions.AuthExceptions.AuthenticationError;
import com.reservationSys.reservationSys.Exceptions.GeneralExceptions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;

    @Value("#{${refresh.token.expiration-days} * 24L * 60L * 60L * 1000L}")
    private Long refreshTokenExpiration;
    @Value("${refresh.token.secret}")
    private String refreshTokenSecret;

    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }




    @Transactional
    public RefreshToken generateRefreshToken(AppUser user){
        RefreshToken generated = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                        .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                                .isRevoked(false)
                                        .createdAt(Instant.now())
                                                .userId(user.getId())
                                                        .build();


        refreshTokenRepo.save(generated);
        return generated;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token){

        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(()-> new ResourceNotFound("Refresh token not found"));

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


    @Transactional
    public RefreshToken rotateRefreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(()-> new ResourceNotFound("Refresh token not found"));
        if (refreshToken.getExpiresAt().isBefore(Instant.now())){
            refreshToken.setIsRevoked(true);
            refreshTokenRepo.save(refreshToken);
            throw new AuthenticationError("Refresh token expired");
        }
        if(refreshToken.getIsRevoked()){
            throw new AuthenticationError("Refresh token revoked");
        }
        // Revoke old token first
        refreshToken.setIsRevoked(true);
        refreshTokenRepo.save(refreshToken);

        // Generate new token with same userId
        RefreshToken newToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .isRevoked(false)
                .createdAt(Instant.now())
                .userId(refreshToken.getUserId())
                .build();
        refreshTokenRepo.save(newToken);
        return newToken;
    }





}
