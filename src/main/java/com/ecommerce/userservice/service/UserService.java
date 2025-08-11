package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.User.RegisterRequest;
import com.ecommerce.userservice.dto.User.UpdateProfileRequest;
import com.ecommerce.userservice.dto.User.UserDTO;

public interface UserService{

    UserDTO register(RegisterRequest request);
    UserDTO getProfileByEmail(String email);
    UserDTO updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, String oldPassword, String newPassword);
    void requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
    void ensureUserExists(String email);
}
