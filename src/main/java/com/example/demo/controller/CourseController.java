package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseContent;
import com.example.demo.entity.Professor;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ProfessorRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseRepository repo;

    public CourseController(CourseRepository repo) {
        this.repo = repo;

    }

    // GET /api/courses
    @GetMapping
    public List<Course> all() {
        return repo.findAll();
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    public Course getOne(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    // POST /api/courses
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Course create(@RequestBody Course courses) {
        return repo.save(courses);
    }

    // PUT /api/courses/{id}
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Course update(@PathVariable Long id, @RequestBody Course updated) {
        Course course = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setAbbreviation(updated.getAbbreviation());
        course.setTitle(updated.getTitle());
        return repo.save(course);
    }



    // DELETE /api/courses/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}