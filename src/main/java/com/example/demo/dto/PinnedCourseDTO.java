package com.example.demo.dto;

import java.time.Instant;

public record PinnedCourseDTO(
        Long id,
        Long courseId,
        String courseTitle,
        String courseCode,
        Instant pinnedAt
) {}