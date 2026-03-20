package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepo;
    private final AnnouncementReadRepository readRepo;
    private final UserRepository userRepo;

    public AnnouncementController(AnnouncementRepository announcementRepo,
                                  AnnouncementReadRepository readRepo,
                                  UserRepository userRepo) {
        this.announcementRepo = announcementRepo;
        this.readRepo = readRepo;
        this.userRepo = userRepo;
    }

    // 1. Fetch announcements (same as supabase query)
    @GetMapping
    public List<AnnouncementResponseDTO> getAnnouncements() {
        return announcementRepo
                .findByExpiresAtIsNullOrExpiresAtAfterOrderByCreatedAtDesc(Instant.now())
                .stream()
                .map(a -> new AnnouncementResponseDTO(
                        a.getId(),
                        a.getTitle(),
                        a.getMessage(),
                        a.getLink(),
                        a.getCreatedAt(),
                        a.getExpiresAt()
                ))
                .toList();
    }

    // 2. Fetch read announcements for user
    @GetMapping("/reads/{userId}")
    public List<AnnouncementReadDTO> getReads(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();

        return readRepo.findByUser(user)
                .stream()
                .map(r -> new AnnouncementReadDTO(r.getAnnouncement().getId()))
                .toList();
    }

    // 3. Mark as read
    @PostMapping("/reads")
    public void markAsRead(@RequestParam Long userId,
                           @RequestParam Long announcementId) {

        User user = userRepo.findById(userId).orElseThrow();

        Announcement a = announcementRepo.findById(announcementId).orElseThrow();


        if (readRepo.existsByUserAndAnnouncement(user, a)) {
            return;
        }

        Announcement announcement = announcementRepo.findById(announcementId).orElseThrow();

        AnnouncementRead read = new AnnouncementRead();
        read.setUser(user);
        read.setAnnouncement(announcement);
        read.setReadAt(Instant.now());

        readRepo.save(read);
    }

    // 4. Create announcement (admin)
    @PostMapping
    public void create(@RequestBody CreateAnnouncementDTO dto,
                       @RequestParam Long userId) {

        User user = userRepo.findById(userId).orElseThrow();

        Announcement a = new Announcement();
        a.setTitle(dto.getTitle());
        a.setMessage(dto.getMessage());
        a.setLink(dto.getLink());
        a.setExpiresAt(dto.getExpiresAt());
        a.setCreatedAt(Instant.now());
        a.setCreatedBy(user);

        announcementRepo.save(a);
    }
}