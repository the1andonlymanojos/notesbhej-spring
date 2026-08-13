package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.UserRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserRestaurant(UserRestaurant userRestaurant);
    List<Review> findByUserRestaurant_Restaurant_IdAndDeletedFalse(Long restaurantId);
    long countByUserRestaurant_Restaurant_IdAndDeletedFalse(Long restaurantId);
}
