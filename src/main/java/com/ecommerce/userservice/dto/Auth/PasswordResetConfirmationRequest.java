package com.ecommerce.userservice.dto.Auth;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmationRequest(@NotBlank String token, @NotBlank String newPassword) {
}
