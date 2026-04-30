package com.example.demo.controller;

import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {


    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // -------- DTO (only fields you allow updating) --------
    public static class UpdateUserRequest {
        public String bgPref;
        public String fullName;
        public String batch;
        public String pfpURL;
        public Boolean adminRequest;
    }
    @GetMapping("/health")
    public Object health() {
        return java.util.Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now().toString()
        );
    }

    // -------- Get current user --------
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO me(@AuthenticationPrincipal User user) {
        return new UserResponseDTO(user.getId(),user.getEmail(), user.getFullName(), user.getBatch(), user.getProfilePictureUrl(), user.getBgPref(), user.getRole(), user.getAdminRequest());
    }

    @GetMapping("/demo")
    public UserResponseDTO demo(@CookieValue(name = "demo_id", required = false) String demoId) {
        if (demoId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No demo cookie");
        }

        UUID userId;
        try {
            userId = UUID.fromString(demoId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid demo_id");
        }

        User user = repo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getBatch(),
                user.getProfilePictureUrl(),
                user.getBgPref(),
                user.getRole(),
                user.getAdminRequest()
        );

    }

    @GetMapping("/logged-in")
    public boolean loggedIn(@AuthenticationPrincipal User user) {
        return user!=null;
   }

    @GetMapping("/me/bg")
    @PreAuthorize("isAuthenticated()")
    public String getBG(@AuthenticationPrincipal User user) {
        return user.getBgPref();
    }

    // -------- Get all users -------- only for debug
//    @GetMapping("/users")
//    public List<User> getthemall() {
//        return repo.findAll();
//    }

    // -------- PATCH current user --------
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO updateMe(
            @AuthenticationPrincipal User userDetails,
            @RequestBody UpdateUserRequest updates
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        if (updates.bgPref != null && !updates.bgPref.isBlank()) {
            userDetails.setBgPref(updates.bgPref);
        }
        if (updates.fullName != null && !updates.fullName.isBlank()) {
            userDetails.setFullName(updates.fullName);
        }
        if(updates.pfpURL!=null && !updates.pfpURL.isBlank()){
            userDetails.setProfilePictureUrl(updates.pfpURL);
        }
        if (updates.batch != null && !updates.batch.isBlank()) {
            userDetails.setBatch(updates.batch);
        }

        if (updates.adminRequest != null ) {
            userDetails.setAdminRequest(updates.adminRequest);
        }


        repo.save(userDetails);
        return new UserResponseDTO(userDetails.getId(),userDetails.getEmail(), userDetails.getFullName(), userDetails.getBatch(), userDetails.getProfilePictureUrl(), userDetails.getBgPref(), userDetails.getRole(), userDetails.getAdminRequest());
    }

    // -------- PATCH by ID (restricted) -------- only for migrations
//    @PatchMapping("/users/{id}")
//    public User updateUserById(
//            @PathVariable Long id,
//            @RequestBody UpdateUserRequest updates
//    ) {
//        User user = repo.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        if (updates.bgPref != null && !updates.bgPref.isBlank()) {
//            user.setBgPref(updates.bgPref);
//        }
//
//        return repo.save(user);
//    }
}