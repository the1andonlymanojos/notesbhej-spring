package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatusAndCuisineIgnoreCaseContainingAndPriceCategory(
            RestaurantStatus status, String cuisine, com.example.demo.entity.RestaurantPriceCategory priceCategory);
    List<Restaurant> findByStatus(RestaurantStatus status);
    Optional<Restaurant> findByIdAndStatus(Long id, RestaurantStatus status);
}
