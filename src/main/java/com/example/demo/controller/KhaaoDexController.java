package com.example.demo.controller;

import com.example.demo.dto.khaao.KhaaoDexDtos.*;
import com.example.demo.entity.RestaurantPriceCategory;
import com.example.demo.entity.RestaurantCategory;
import com.example.demo.entity.User;
import com.example.demo.service.KhaaoDexService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/khaao-dex")
public class KhaaoDexController {
    private final KhaaoDexService service;

    public KhaaoDexController(KhaaoDexService service) {
        this.service = service;
    }

    @GetMapping("/restaurants")
    public List<RestaurantView> discover(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) RestaurantPriceCategory priceCategory,
            @RequestParam(required = false) Set<RestaurantCategory> categories,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) Boolean visited) {
        return service.discover(user, cuisine, priceCategory, categories, latitude, longitude, radiusKm, visited);
    }

    @GetMapping("/restaurants/{id}")
    public RestaurantDetailView get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return service.getDetail(id, user);
    }

    @PostMapping("/restaurants")
    @PreAuthorize("isAuthenticated()")
    public RestaurantView submit(@Valid @RequestBody RestaurantSubmissionRequest request,
                                 @AuthenticationPrincipal User user) {
        return service.submit(request, user);
    }

    @PutMapping("/restaurants/{id}/relationship")
    @PreAuthorize("isAuthenticated()")
    public RelationshipView relationship(@PathVariable Long id, @Valid @RequestBody RelationshipRequest request,
                                         @AuthenticationPrincipal User user) {
        return service.saveRelationship(id, request, user);
    }

    @PutMapping("/restaurants/{id}/review")
    @PreAuthorize("isAuthenticated()")
    public ReviewView review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request,
                             @AuthenticationPrincipal User user) {
        return service.saveReview(id, request, user);
    }

    @DeleteMapping("/restaurants/{id}/review")
    @PreAuthorize("isAuthenticated()")
    public void deleteReview(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.deleteReview(id, user);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public MyDexView myDex(@AuthenticationPrincipal User user) {
        return service.myDex(user);
    }

    @PostMapping("/restaurants/{id}/edits")
    @PreAuthorize("isAuthenticated()")
    public RestaurantEditView submitEdit(@PathVariable Long id, @Valid @RequestBody RestaurantEditRequest request,
                                         @AuthenticationPrincipal User user) {
        return service.submitEdit(id, request, user);
    }

    @GetMapping("/moderation/restaurants")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public List<RestaurantView> pendingRestaurants() {
        return service.pendingRestaurants();
    }

    @PatchMapping("/moderation/restaurants/{id}/approve")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public RestaurantView approveRestaurant(@PathVariable Long id, @Valid @RequestBody(required = false) ModerationRequest request,
                                             @AuthenticationPrincipal User user) {
        return service.moderateRestaurant(id, true, request, user);
    }

    @PatchMapping("/moderation/restaurants/{id}/reject")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public RestaurantView rejectRestaurant(@PathVariable Long id, @Valid @RequestBody(required = false) ModerationRequest request,
                                           @AuthenticationPrincipal User user) {
        return service.moderateRestaurant(id, false, request, user);
    }

    @GetMapping("/moderation/edits")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public List<RestaurantEditView> pendingEdits() {
        return service.pendingEdits();
    }

    @PatchMapping("/moderation/edits/{id}/approve")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public RestaurantEditView approveEdit(@PathVariable Long id, @Valid @RequestBody(required = false) ModerationRequest request,
                                           @AuthenticationPrincipal User user) {
        return service.moderateEdit(id, true, request, user);
    }

    @PatchMapping("/moderation/edits/{id}/reject")
    @PreAuthorize("hasAnyRole('MODERATOR','ADMIN')")
    public RestaurantEditView rejectEdit(@PathVariable Long id, @Valid @RequestBody(required = false) ModerationRequest request,
                                         @AuthenticationPrincipal User user) {
        return service.moderateEdit(id, false, request, user);
    }
}
