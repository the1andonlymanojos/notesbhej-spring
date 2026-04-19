package com.example.demo.dto;

public record LeaderboardDTO(
        Long userId,
        String username,
        Long contributionCount
) {}
