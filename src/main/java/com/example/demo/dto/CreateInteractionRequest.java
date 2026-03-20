package com.example.demo.dto;

public record CreateInteractionRequest(
        Long courseId,
        Long contentId,
        String type,
        String message
) {}
