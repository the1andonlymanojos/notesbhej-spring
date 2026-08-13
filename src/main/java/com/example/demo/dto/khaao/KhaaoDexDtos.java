package com.example.demo.dto.khaao;

import com.example.demo.entity.RestaurantEditStatus;
import com.example.demo.entity.RestaurantPriceCategory;
import com.example.demo.entity.RestaurantStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public final class KhaaoDexDtos {
    private KhaaoDexDtos() {}

    public record RestaurantSubmissionRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 500) String address,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @Size(max = 100) String cuisine,
            RestaurantPriceCategory priceCategory,
            @Size(max = 200) String googlePlaceId) {}

    public record RelationshipRequest(@NotNull Boolean visited) {}

    public record ReviewRequest(
            @Min(1) @Max(5) Integer overallRating,
            @Min(1) @Max(5) Integer valueForMoneyRating,
            @Min(1) @Max(5) Integer foodQualityRating,
            @Min(1) @Max(5) Integer ambienceRating,
            @Size(max = 4000) String text,
            @Size(max = 10) List<@NotBlank @Size(max = 1000) String> imageUrls) {}

    public record RestaurantEditRequest(
            @Size(max = 160) String name,
            @Size(max = 500) String address,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @Size(max = 100) String cuisine,
            RestaurantPriceCategory priceCategory,
            @Size(max = 200) String googlePlaceId) {}

    public record ModerationRequest(@Size(max = 1000) String note) {}

    public record RelationshipView(Long id, boolean visited, ReviewView review) {}

    public record ReviewView(Long id, Long userId, String userName, Integer overallRating,
                             Integer valueForMoneyRating, Integer foodQualityRating,
                             Integer ambienceRating, String text, List<String> imageUrls,
                             Instant createdAt, Instant updatedAt) {}

    public record RestaurantView(Long id, String name, String address, Double latitude,
                                 Double longitude, String cuisine,
                                 RestaurantPriceCategory priceCategory, String googlePlaceId,
                                 RestaurantStatus status, RelationshipView relationship,
                                 Double averageRating, long reviewCount) {}

    public record RestaurantDetailView(RestaurantView restaurant, List<ReviewView> reviews) {}

    public record RestaurantEditView(Long id, Long restaurantId, Long submittedById,
                                     RestaurantEditStatus status, RestaurantEditRequest proposed,
                                     Instant createdAt, Long reviewedById, Instant reviewedAt,
                                     String moderationNote) {}

    public record MyDexView(long visitedCount, long totalActiveRestaurants,
                            double explorationPercentage, List<RestaurantView> visitedRestaurants) {}
}
