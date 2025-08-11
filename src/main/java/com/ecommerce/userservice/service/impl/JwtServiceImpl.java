package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.repository.PasswordResetTokenRepository;
import com.ecommerce.userservice.service.JwtService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.issuer}") private String issuer;
    @Value("${jwt.access-exp-min}") private long accessExpMin;
    @Value("${jwt.refresh-exp-days}") private long refreshExpDays;


    private SecretKey key(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateToken(UserDetails principal) {
        return generateAccess(principal.getUsername());
    }

    @Override
    public String generateToken(String username) {
        return generateAccess(username);
    }

    private String generateAccess(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpMin*360)))
                .signWith(key())
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpDays*24*3600)))
                .signWith(key())
                .compact();
    }

    @Override
    public String rotateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpDays*24*3600)))
                .signWith(key())
                .compact();
    }

    @Override
    public String extractUserName(String token) {
        return Jwts.parser()
                .verifyWith(key()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public void validateRefreshToken(String token) {
        Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails principal) {
        try{
            String sub = extractUserName(token);
            return sub.equals(principal.getUsername());
        } catch (JwtException e){return false;}
    }
}
