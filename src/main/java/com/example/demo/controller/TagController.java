package com.example.demo.controller;

import com.example.demo.entity.Tag;
import com.example.demo.repository.TagRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagRepository repo;

    public TagController(TagRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Tag create(@RequestBody Tag tag) {
        return repo.save(tag);
    }

    @GetMapping
    public List<Tag> all() {
        return repo.findAll();
    }
}
