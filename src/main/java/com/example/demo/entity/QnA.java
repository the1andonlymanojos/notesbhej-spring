package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class QnA {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String question;

    private String response;

    private Boolean yesNo;

    private Integer rating;

    private Instant createdAt;
}
