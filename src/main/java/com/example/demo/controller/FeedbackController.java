package com.example.demo.controller;

import com.example.demo.dto.FeedbackRequest;
import com.example.demo.entity.Feedback;
import com.example.demo.entity.User;
import com.example.demo.repository.FeedbackRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackRepository repo;

    public FeedbackController(FeedbackRepository repo) {
        this.repo = repo;
    }
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Feedback create(
            @RequestBody FeedbackRequest req,
            @AuthenticationPrincipal User user
    ) {
        Feedback f = new Feedback();
        f.setUser(user);
        f.setFeedback(req.feedback());
        f.setRating(req.rating());
        f.setCreatedAt(Instant.now());
        return repo.save(f);
    }
}