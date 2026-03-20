package com.example.demo.dto;

import java.time.Instant;

public record InteractionDTO(
        Long courseId,
        String courseTitle,
        Long contentId,
        String contentTitle,
        String type,
        Instant createdAt
) {}


