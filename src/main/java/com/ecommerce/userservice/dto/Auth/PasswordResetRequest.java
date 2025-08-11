package com.ecommerce.userservice.dto.Auth;

import jakarta.validation.constraints.Email;

public record PasswordResetRequest(@Email String email) {
}
