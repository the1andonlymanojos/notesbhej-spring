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
public class Feedback {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String feedback;

    private Integer rating;

    private Instant createdAt;
}
