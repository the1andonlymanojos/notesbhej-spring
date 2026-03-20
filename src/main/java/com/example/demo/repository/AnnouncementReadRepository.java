package com.example.demo.repository;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.AnnouncementRead;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {

    List<AnnouncementRead> findByUser(User user);

    boolean existsByUserAndAnnouncement(User user, Announcement announcement);
}
