package com.example.demo.dto;

import com.example.demo.entity.UserRole;

public record ProfessorDTO(
        Long id,
        String name,
        String department,
        String designation
) {}

