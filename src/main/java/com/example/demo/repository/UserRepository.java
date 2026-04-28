package com.example.demo.repository;

import com.example.demo.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);
 }



