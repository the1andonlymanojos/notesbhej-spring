package com.example.demo.controller;

import com.example.demo.dto.CreateInteractionRequest;
import com.example.demo.dto.InteractionDTO;
import com.example.demo.dto.InteractionMapper;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseContent;
import com.example.demo.entity.User;
import com.example.demo.entity.UserCourseInteraction;
import com.example.demo.repository.CourseContentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserCourseInteractionRepository;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interactions")
public class UserCourseInteractionController {

    private final UserCourseInteractionRepository repo;
    private final CourseRepository courseRepo;
    private final CourseContentRepository courseContentRepository;

    public UserCourseInteractionController(
            UserCourseInteractionRepository repo,
            CourseRepository courseRepo, CourseContentRepository courseContentRepository
    ) {
        this.repo = repo;
        this.courseRepo = courseRepo;
        this.courseContentRepository = courseContentRepository;
    }

    // recent activity across all courses
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<InteractionDTO> myRecent(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return repo.findAllByUser(
                        user,
                        Sort.by(Sort.Direction.DESC, "createdAt"),
                        Limit.of(limit)
                )
                .stream()
                .map(InteractionMapper::toDTO)
                .toList();
    }

    // recent content inside a specific course
    @GetMapping("/me/course/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public List<InteractionDTO> myCourseRecent(
            @AuthenticationPrincipal User user,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "10") int limit
    ) {

        Course course = courseRepo.findById(courseId).orElseThrow();

        return repo.findAllByUserAndCourse(
                        user,
                        course,
                        Sort.by(Sort.Direction.DESC, "createdAt"),
                        Limit.of(limit)
                )
                .stream()
                .map(InteractionMapper::toDTO)
                .toList();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InteractionDTO createInteraction(
            @AuthenticationPrincipal User user,
            @RequestBody CreateInteractionRequest req
    ) {

        Course course = null;
        CourseContent content = null;

        if (req.contentId() != null) {
            content = courseContentRepository.findById(req.contentId()).orElseThrow();
            course = content.getCourse(); // infer course from content
        }

        if (req.courseId() != null) {
            course = courseRepo.findById(req.courseId()).orElseThrow();
        }
        System.out.println("....... req ");
        System.out.println(req.message());

        UserCourseInteraction interaction = new UserCourseInteraction();
        interaction.setUser(user);
        interaction.setCourse(course);
        interaction.setContent(content);
        interaction.setMessage(req.message());
        interaction.setCreatedAt(java.time.Instant.now());

        UserCourseInteraction saved = repo.save(interaction);

        return InteractionMapper.toDTO(saved);
    }
}

