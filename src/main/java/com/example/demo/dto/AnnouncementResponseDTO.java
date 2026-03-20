package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AnnouncementResponseDTO {
    private Long id;
    private String title;
    private String message;
    private String link;
    private Instant createdAt;
    private Instant expiresAt;

    public AnnouncementResponseDTO(Long id, String title, String message,
                                   String link, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.link = link;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}

