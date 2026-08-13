package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "khaao_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_khaao_review_user_restaurant", columnNames = "user_restaurant_id"))
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_restaurant_id", nullable = false, unique = true)
    private UserRestaurant userRestaurant;

    private Integer overallRating;

    private Integer valueForMoneyRating;
    private Integer foodQualityRating;
    private Integer ambienceRating;

    @Column(length = 4000)
    private String text;

    @ElementCollection
    @CollectionTable(name = "khaao_review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
