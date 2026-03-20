package com.example.demo.controller;

import com.example.demo.dto.PinnedCourseDTO;
import com.example.demo.entity.Course;
import com.example.demo.entity.PinnedCourses;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.PinnedCoursesRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pinned-courses")
public class PinnedCoursesController {

    private final PinnedCoursesRepository repo;
    private final CourseRepository courseRepository;

    public PinnedCoursesController(PinnedCoursesRepository repo, CourseRepository courseRepository) {
        this.repo = repo;
        this.courseRepository = courseRepository;
    }

//    @GetMapping
//    public List<PinnedCourses> all() {
//        return repo.findAll();
//    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<PinnedCourseDTO> me(@AuthenticationPrincipal User user) {
        return repo.findPinnedCoursesForUser(user.getId());
    }

//    @PostMapping
//    @PreAuthorize("isAuthenticated()")
//    public PinnedCourses create(@RequestBody PinnedCourses pin, @AuthenticationPrincipal User user) {
//        if(pin.getUser()!=user) throw new BadRequestException("Yo wrong user");
//        PinnedCourses saved = repo.save(pin);
//        return repo.findById(saved.getId()).orElseThrow();
//    }

    @PostMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public void pin(@PathVariable Long courseId,
                    @AuthenticationPrincipal User user) {


        Course course = courseRepository.findById(courseId)
                .orElseThrow();

        if (repo.findByUserAndCourse(user, course).isPresent()) {
            return;
        }

        PinnedCourses pin = new PinnedCourses();
        pin.setUser(user);
        pin.setCourse(course);
        pin.setPinnedAt(Instant.now());

        repo.save(pin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {

        long start = System.nanoTime();
        PinnedCourses pinnedCourses = repo.findById(id)
                .orElseThrow();
        if(pinnedCourses.getUser()!=user) throw new BadRequestException("you aint the user!");
        repo.deleteById(id);
    }
}

