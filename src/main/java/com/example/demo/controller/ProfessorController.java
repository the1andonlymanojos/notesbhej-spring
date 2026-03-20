package com.example.demo.controller;

import com.example.demo.dto.ProfessorCourseDTO;
import com.example.demo.entity.Professor;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CourseContentRepository;
import com.example.demo.repository.ProfessorRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/professors")
public class ProfessorController {

    private final ProfessorRepository repo;
    private final CourseContentRepository courseContentRepository;

    public ProfessorController(ProfessorRepository repo, CourseContentRepository courseContentRepository) {
        this.repo = repo;

        this.courseContentRepository = courseContentRepository;
    }

    // GET /api/professors
    @GetMapping
    public List<Professor> all() {
        return repo.findAll();
    }

    // GET /api/professors/{id}
    @GetMapping("/{id}")
    public Professor getOne(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Professor not found"));
    }

    // POST /api/professors
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Professor create(@RequestBody Professor professor) {
        return repo.save(professor);
    }

    // PUT /api/professors/{id}
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Professor update(@PathVariable Long id, @RequestBody Professor updated) {

        Professor professor = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Professor not found"));

        professor.setName(updated.getName());
        professor.setDepartment(updated.getDepartment());
        professor.setEmail(updated.getEmail());

        return repo.save(professor);
    }

    // DELETE /api/professors/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping("/courses")
    public List<ProfessorCourseDTO> professorCourses(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit
    ) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return courseContentRepository.findProfessorCourses(pageable);
    }
}