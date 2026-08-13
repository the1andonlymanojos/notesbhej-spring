package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRestaurantRepository extends JpaRepository<UserRestaurant, Long> {
    Optional<UserRestaurant> findByUserAndRestaurant(User user, Restaurant restaurant);
    Optional<UserRestaurant> findByUserIdAndRestaurantId(Long userId, Long restaurantId);
    List<UserRestaurant> findByUserAndVisitedTrueAndRestaurant_Status(User user, com.example.demo.entity.RestaurantStatus status);
    long countByUserAndVisitedTrueAndRestaurant_Status(User user, com.example.demo.entity.RestaurantStatus status);
}
