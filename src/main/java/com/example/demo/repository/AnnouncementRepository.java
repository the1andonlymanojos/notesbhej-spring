package com.example.demo.repository;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.AnnouncementRead;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByExpiresAtIsNullOrExpiresAtAfterOrderByCreatedAtDesc(Instant now);
}



