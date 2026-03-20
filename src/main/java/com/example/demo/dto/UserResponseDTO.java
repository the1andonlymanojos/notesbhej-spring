package com.example.demo.dto;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;

public record UserResponseDTO(
        Long id,
        String email,
        String fullName,
        String batch,
        String profilePictureUrl,
        String bgPref,
        UserRole role,
        Boolean userReq
) {}
