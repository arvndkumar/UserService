package com.ecommerce.userservice.dto.Auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@Email String email, @NotBlank String password) {

}
