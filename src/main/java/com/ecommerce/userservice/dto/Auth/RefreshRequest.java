package com.ecommerce.userservice.dto.Auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {

}
