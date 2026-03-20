package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Announcement {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String message;

    private String link;

    private Instant createdAt;

    private Instant expiresAt;

    @ManyToOne
    private User createdBy;
}
