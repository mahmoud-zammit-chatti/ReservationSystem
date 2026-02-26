package com.reservationSys.reservationSys.Services.auth;


import com.reservationSys.reservationSys.exceptions.AuthenticationError;
import com.reservationSys.reservationSys.security.MyAppUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private int jwtExpiration;

    public String generateToken(Map<String, Object> claims, String email) {

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(getSigningKey())
                .compact();


    }
    public Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public <T> T extractClaims(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }
    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    public void validateToken(String token, MyAppUserDetails userDetails){
        String username = extractUsername(token);
        Date expiration = extractExpiration(token);
        if( expiration==null || expiration.before(new Date())){
            throw new AuthenticationError("Token expired");
        }
        if( username==null || !username.equals(userDetails.getUsername())){
            throw new BadCredentialsException("Invalid token");
        }
    }


}
