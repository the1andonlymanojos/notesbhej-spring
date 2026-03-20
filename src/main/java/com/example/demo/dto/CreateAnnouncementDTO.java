package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateAnnouncementDTO {
    private String title;
    private String message;
    private String link;
    private Instant expiresAt;

    // getters/setters
}


