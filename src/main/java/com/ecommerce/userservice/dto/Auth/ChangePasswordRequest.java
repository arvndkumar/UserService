package com.ecommerce.userservice.dto.Auth;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {
}
