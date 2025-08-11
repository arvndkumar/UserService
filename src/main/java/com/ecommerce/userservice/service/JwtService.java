package com.ecommerce.userservice.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserDetails principal);

    String generateToken(String username);

    String generateRefreshToken(UserDetails principal);

    String rotateRefreshToken(String username);

    String extractUserName(String token);

    void validateRefreshToken(String token);

    boolean isTokenValid(String token, UserDetails principal);
}


