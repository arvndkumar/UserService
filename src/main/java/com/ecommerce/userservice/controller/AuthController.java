package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.Auth.*;
import com.ecommerce.userservice.dto.User.RegisterRequest;
import com.ecommerce.userservice.dto.User.UpdateProfileRequest;
import com.ecommerce.userservice.dto.User.UserDTO;
import com.ecommerce.userservice.service.JwtService;
import com.ecommerce.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserDetails principal = (UserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@RequestHeader("Authorization") String bearer) {
        String email = jwtService.extractUserName(bearer.substring(7));
        return ResponseEntity.ok(userService.getProfileByEmail(email));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        String username = jwtService.extractUserName(req.refreshToken());
        userService.ensureUserExists(username);
        jwtService.validateRefreshToken(req.refreshToken());
        String newAccess = jwtService.generateToken(username);
        String newRefresh = jwtService.rotateRefreshToken(username);
        return ResponseEntity.ok(new AuthResponse(newAccess, newRefresh));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest req,
                                               @RequestHeader("Authorization") String bearer) {
        String email = jwtService.extractUserName(bearer.substring(7));
        userService.changePassword(email, req.oldPassword(), req.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/request")
    public ResponseEntity<Void> requestReset(@RequestBody PasswordResetRequest req) {
        userService.requestPasswordReset(req.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<Void> confirmReset(@RequestBody PasswordResetConfirmationRequest req) {
        userService.confirmPasswordReset(req.token(), req.newPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestHeader("Authorization") String bearer,
                                                 @RequestBody UpdateProfileRequest req) {
        String email = jwtService.extractUserName(bearer.substring(7));
        return ResponseEntity.ok(userService.updateProfile(email,req));
    }
}