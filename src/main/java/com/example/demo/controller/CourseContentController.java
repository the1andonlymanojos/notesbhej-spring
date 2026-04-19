package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CourseContentRepository;
import com.example.demo.repository.ProfessorRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/course-content")
public class CourseContentController {

    private final CourseContentRepository repo;
  //  private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;
    private final TagRepository tagRepository;

    public CourseContentController(CourseContentRepository repo, ProfessorRepository professorRepository, TagRepository tagRepository) {
        this.repo = repo;
        this.professorRepository = professorRepository;
        this.tagRepository = tagRepository;
    }

    // GET all
//    @GetMapping("/all")
//    public List<CourseContent> all() {
//        return repo.findAll();
//    }


    @GetMapping("/{id}")
    public CourseContentResponse getForCourse(@PathVariable Long id,   @AuthenticationPrincipal User user) {
        boolean isAuthenticated = user != null;
        List<CourseContent> entities = repo.findAllByCourse_IdOrderByOrderIndexAsc(id);

        List<CourseContentDTO> content = entities.stream()
                .filter(c -> canUserSee(c, user))
                .map(courseContent -> CourseContentMapper.toDTO(courseContent, isAuthenticated))
                .toList();

        Map<Long, ProfessorDTO> professors = entities.stream()
                .map(CourseContent::getProfessor)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Professor::getId,
                        CourseContentMapper::toProfessorDTO,
                        (a, b) -> a
                ));

        return new CourseContentResponse(content, professors);
    }


    @GetMapping("/me/{id}")
    @PreAuthorize("isAuthenticated()")
    public CourseContentResponse getForUser(@PathVariable Long id,   @AuthenticationPrincipal User user) {

        List<CourseContent> entities = repo.findAllByUploadedBy(user);

        List<CourseContentDTO> content = entities.stream()
                .filter(c -> canUserSee(c, user))
                .map(courseContent -> CourseContentMapper.toDTO(courseContent, true))
                .toList();

        Map<Long, ProfessorDTO> professors = entities.stream()
                .map(CourseContent::getProfessor)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Professor::getId,
                        CourseContentMapper::toProfessorDTO,
                        (a, b) -> a
                ));

        return new CourseContentResponse(content, professors);
    }



    // CREATE
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CourseContent create(@RequestBody CourseContent content, @AuthenticationPrincipal User user) {
        Double maxOrder = repo.findMaxOrderInGroup(content.getCourse().getId(), content.getYear(), content.getSemesterNumber(), content.getBatch(), content.getProfessor().getId());
        content.setOrderIndex(maxOrder == null ? 1000 : maxOrder + 1000);
        content.setUploadedBy(user);
        content.setVisibility(user.getRole()==UserRole.ADMIN?ContentVisibility.VISIBLE:ContentVisibility.PENDING_REVIEW);
        content.setIsLatest(true);
        content.setCreatedAt(Instant.now());
        content.setUpdatedAt(Instant.now());
        return repo.save(content);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CourseContent update(
            @PathVariable Long id,
            @RequestBody CourseContent updated,
            @AuthenticationPrincipal User user
    ) {
        CourseContent existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Content not found"));

        // overwrite fields
        existing.setTitle(updated.getTitle());
        existing.setBatch(updated.getBatch());
        existing.setYear(updated.getYear());
        existing.setSemesterNumber(updated.getSemesterNumber());
        existing.setResourceUrl(updated.getResourceUrl());
        existing.setR2Url(updated.getR2Url());
        existing.setFileType(updated.getFileType());

        existing.setCourse(updated.getCourse());
        existing.setProfessor(updated.getProfessor());
        existing.setTags(updated.getTags());

        existing.setUpdatedAt(Instant.now());

        existing.setVisibility(
                user.getRole() == UserRole.ADMIN
                        ? ContentVisibility.VISIBLE
                        : ContentVisibility.PENDING_REVIEW
        );

        return repo.save(existing);
    }


    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        CourseContent content = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        content.setVisibility(ContentVisibility.DELETED);
        repo.save(content);
    }



    private boolean canUserSee(CourseContent c, User user) {

        if(user==null) return c.getVisibility() == ContentVisibility.VISIBLE;
        // ADMIN: everything except deleted
        if (user.getRole() == UserRole.ADMIN) {
            return c.getVisibility() != ContentVisibility.DELETED;
        }

        // Always allow visible content
        if (c.getVisibility() == ContentVisibility.VISIBLE) {
            return true;
        }

        // Uploader can see their own pending content
        if (c.getVisibility() == ContentVisibility.PENDING_REVIEW &&
                c.getUploadedBy() != null &&
                c.getUploadedBy().getId().equals(user.getId())) {
            return true;
        }

        return false;
    }

    @PatchMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean patchVis(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody PatchVisDto patchVisDto
    ) {

        CourseContent content = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        content.setVisibility(patchVisDto.visibility());

        repo.save(content);

        return true;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CourseContent patch(
            @PathVariable Long id,
            @RequestBody CourseContentPatchDTO patch,
            @AuthenticationPrincipal User user
    ) {
        CourseContent existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found"));

        if (patch.title() != null) existing.setTitle(patch.title());
        if (patch.year() != null) existing.setYear(patch.year());
        if (patch.batch() != null) existing.setBatch(patch.batch());
        if (patch.semesterNumber() != null) existing.setSemesterNumber(patch.semesterNumber());

        if (patch.professorId() != null) {
            Professor prof = professorRepository.findById(patch.professorId())
                    .orElseThrow(() -> new NotFoundException("Professor not found"));
            existing.setProfessor(prof);
        }

        if (patch.tagIds() != null) {
            existing.setTags(tagRepository.findAllById(patch.tagIds()));
        }

        existing.setUpdatedAt(Instant.now());

        existing.setVisibility(
                user.getRole() == UserRole.ADMIN
                        ? ContentVisibility.VISIBLE
                        : ContentVisibility.PENDING_REVIEW
        );

        return repo.save(existing);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseContentResponse getPendingReview(@AuthenticationPrincipal User user) {

        List<CourseContent> entities =
                repo.findAllByVisibility(ContentVisibility.PENDING_REVIEW);

        List<CourseContentDTO> content = entities.stream()
                .map(courseContent -> CourseContentMapper.toDTO(courseContent, true))
                .toList();

        Map<Long, ProfessorDTO> professors = entities.stream()
                .map(CourseContent::getProfessor)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Professor::getId,
                        CourseContentMapper::toProfessorDTO,
                        (a, b) -> a
                ));

        return new CourseContentResponse(content, professors);
    }

    @PatchMapping("/{id}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseContent reorder(
            @PathVariable Long id,
            @RequestBody ReorderDTO dto
    ) {
        CourseContent item = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        Double newOrder;

        if (dto.prevId() == null && dto.nextId() == null) {
            throw new RuntimeException("Invalid move");
        }

        if (dto.prevId() == null) {
            CourseContent next = repo.findById(dto.nextId()).orElseThrow();
            newOrder = next.getOrderIndex() - 1000;
        }
        else if (dto.nextId() == null) {
            CourseContent prev = repo.findById(dto.prevId()).orElseThrow();
            newOrder = prev.getOrderIndex() + 1000;
        }
        else {
            CourseContent prev = repo.findById(dto.prevId()).orElseThrow();
            CourseContent next = repo.findById(dto.nextId()).orElseThrow();

            newOrder = (prev.getOrderIndex() + next.getOrderIndex()) / 2;
        }

        item.setOrderIndex(newOrder);
        return repo.save(item);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardDTO> leaderboard() {
        return repo.getLeaderboard();
    }
}



