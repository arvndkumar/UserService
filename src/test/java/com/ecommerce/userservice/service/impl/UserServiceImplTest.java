package com.ecommerce.userservice.service.impl;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private  UserRepository userRepository;
    private  PasswordEncoder passwordEncoder;
    private  RoleRepository roleRepository;
    private  PasswordResetTokenRepository passwordResetTokenRepository;
    private ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;

    private UserServiceImpl service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        objectMapper = mock(ObjectMapper.class);
        kafkaTemplate = mock(KafkaTemplate.class);

        service = new UserServiceImpl(userRepository, passwordEncoder, roleRepository, passwordResetTokenRepository, objectMapper, kafkaTemplate);
    }

    @Test
    void register_success() throws Exception {
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role("ROLE_USER")));
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"dummy\":true}");
        when(kafkaTemplate.send(eq("sendEmail"), anyString())).thenReturn(null);

        User user = new User();
        user.setId(1L);
        user.setName("Arvind");
        user.setEmail("a@a.com");
        user.setPassword("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO dto = service.register(new RegisterRequest("Arvind","a@a.com","pass"));

        assertEquals(1L, dto.id());
        assertEquals("Arvind", dto.name());
        assertEquals("a@a.com", dto.email());

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        assertEquals("hashed", cap.getValue().getPassword());

        verify(kafkaTemplate).send(eq("sendEmail"), anyString());
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);
        assertThrows(BadRequestException.class,
                () -> service.register(new RegisterRequest("A","a@a.com","p")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getProfileByEmail_found() {
        when(userRepository.findByEmail("a@a.com"))
                .thenReturn(Optional.of(User.builder().id(7L).name("A").email("a@a.com").build()));

        UserDTO dto = service.getProfileByEmail("a@a.com");
        assertEquals(7L, dto.id());
        assertEquals("a@a.com", dto.email());
    }

    @Test
    void getProfileByEmail_missing_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProfileByEmail("x@x.com"));
    }

    @Test
    void updateProfile_changesName() {
        User u = User.builder().id(1L).email("a@a.com").name("Old").build();
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));

        UserDTO dto = service.updateProfile("a@a.com", new UpdateProfileRequest("New Name"));

        assertEquals("New Name", dto.name());
        assertEquals("New Name", u.getName()); // in-entity changed
    }

    @Test
    void changePassword_ok() {
        User u = User.builder().id(1L).email("a@a.com").password("hashed-old").build();
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "hashed-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("hashed-new");

        service.changePassword("a@a.com", "old", "new");

        assertEquals("hashed-new", u.getPassword());
    }

    @Test
    void changePassword_wrongOld_throws() {
        User u = User.builder().id(1L).email("a@a.com").password("hashed-old").build();
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "hashed-old")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.changePassword("a@a.com","bad","new"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void requestPasswordReset_savesToken() {
        when(userRepository.findByEmail("a@a.com"))
                .thenReturn(Optional.of(User.builder().email("a@a.com").build()));

        service.requestPasswordReset("a@a.com");

        ArgumentCaptor<PasswordResetToken> cap = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(cap.capture());

        PasswordResetToken saved = cap.getValue();
        assertEquals("a@a.com", saved.getEmail());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void confirmPasswordReset_ok() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(null, token, "a@a.com",
                Instant.now().plusSeconds(1800));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(prt));
        User u = User.builder().email("a@a.com").password("old").build();
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.encode("newpass")).thenReturn("hashed-new");

        service.confirmPasswordReset(token, "newpass");

        assertEquals("hashed-new", u.getPassword());
        verify(passwordResetTokenRepository).delete(prt);
    }

    @Test
    void confirmPasswordReset_invalidToken_throws() {
        when(passwordResetTokenRepository.findByToken("bad")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset("bad","x"));
    }

    @Test
    void confirmPasswordReset_expired_throws() {
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(null, token, "a@a.com",
                Instant.now().minusSeconds(5));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(prt));
        assertThrows(BadRequestException.class, () -> service.confirmPasswordReset(token,"x"));
    }

    @Test
    void ensureUserExists_missing_throws() {
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.ensureUserExists("a@a.com"));
    }

    private Role role(String name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }
}