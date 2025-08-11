package com.ecommerce.userservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.userservice.dto.User.RegisterRequest;
import com.ecommerce.userservice.dto.User.UpdateProfileRequest;
import com.ecommerce.userservice.dto.User.UserDTO;
import com.ecommerce.userservice.exception.BadRequestException;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.model.PasswordResetToken;
import com.ecommerce.userservice.model.Role;
import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.PasswordResetTokenRepository;
import com.ecommerce.userservice.repository.RoleRepository;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Override
    public UserDTO register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Role not found"));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of(role))
                .build();

        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfileByEmail(String email) {
       return userRepository.findByEmail(email).map(this::toDTO)
               .orElseThrow(() -> new ResourceNotFoundException("User Not found"));
    }

    @Override
    public UserDTO updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        if (request.name() != null && !request.name().isBlank()){
            user.setName(request.name().trim());
        }
        return toDTO(user);
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        if(!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new BadRequestException("Old password does not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));

    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(new PasswordResetToken(null,token, user.getEmail(), Instant.now().plus(30, ChronoUnit.MINUTES)));

    }

    @Override
    public void confirmPasswordReset(String token, String newPassword) {

        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("invalid token"));

        if(prt.getExpiresAt().isBefore(Instant.now())){
            throw new BadRequestException("Token expired");
        }

        User user = userRepository.findByEmail(prt.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.delete(prt);
        }

    @Override
    public void ensureUserExists(String email) {
        if(!userRepository.existsByEmail(email)){
            throw new ResourceNotFoundException("User Not found");
        }
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
