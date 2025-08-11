package com.ecommerce.userservice.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor

public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String token;

    @Column(nullable=false)
    private String email;

    @Column(name = "expires_at", nullable=false)
    private Instant expiresAt;
}
