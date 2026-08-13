package com.example.demo.repository;

import com.example.demo.entity.RestaurantEdit;
import com.example.demo.entity.RestaurantEditStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantEditRepository extends JpaRepository<RestaurantEdit, Long> {
    List<RestaurantEdit> findByStatusOrderByCreatedAtAsc(RestaurantEditStatus status);
}
