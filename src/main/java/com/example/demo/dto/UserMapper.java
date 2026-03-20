package com.example.demo.dto;

import com.example.demo.entity.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getBatch(),
                user.getProfilePictureUrl(),
                user.getBgPref(),
                user.getRole(),
                user.getAdminRequest()
        );
    }
}
