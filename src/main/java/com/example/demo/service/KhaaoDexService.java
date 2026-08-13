package com.example.demo.service;

import com.example.demo.dto.khaao.KhaaoDexDtos.*;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class KhaaoDexService {
    private final RestaurantRepository restaurants;
    private final UserRestaurantRepository relationships;
    private final ReviewRepository reviews;
    private final RestaurantEditRepository edits;
    private final double defaultLatitude;
    private final double defaultLongitude;

    public KhaaoDexService(RestaurantRepository restaurants, UserRestaurantRepository relationships,
                           ReviewRepository reviews, RestaurantEditRepository edits,
                           @Value("${khaao-dex.discovery.default-latitude:26.2025}") double defaultLatitude,
                           @Value("${khaao-dex.discovery.default-longitude:78.1746}") double defaultLongitude) {
        this.restaurants = restaurants;
        this.relationships = relationships;
        this.reviews = reviews;
        this.edits = edits;
        this.defaultLatitude = defaultLatitude;
        this.defaultLongitude = defaultLongitude;
    }

    @Transactional
    public RestaurantView submit(RestaurantSubmissionRequest request, User user) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name().trim());
        restaurant.setAddress(request.address().trim());
        restaurant.setLatitude(request.latitude());
        restaurant.setLongitude(request.longitude());
        restaurant.setCuisine(blankToNull(request.cuisine()));
        restaurant.setPriceCategory(request.priceCategory());
        restaurant.setCategories(request.categories() == null ? Set.of() : Set.copyOf(request.categories()));
        restaurant.setGooglePlaceId(blankToNull(request.googlePlaceId()));
        restaurant.setStatus(RestaurantStatus.PENDING);
        restaurant.setCreatedBy(user);
        return toView(restaurants.save(restaurant), user);
    }

    @Transactional
    public List<RestaurantView> discover(User user, String cuisine, RestaurantPriceCategory price,
                                         Set<RestaurantCategory> categories,
                                         Double latitude, Double longitude, Double radiusKm,
                                         Boolean visited) {
        double centerLat = latitude == null ? defaultLatitude : latitude;
        double centerLon = longitude == null ? defaultLongitude : longitude;
        validateCoordinate(centerLat, centerLon);
        if (radiusKm != null && (radiusKm < 0 || radiusKm > 1000)) {
            throw new BadRequestException("radiusKm must be between 0 and 1000");
        }

        return restaurants.findByStatus(RestaurantStatus.ACTIVE).stream()
                .filter(r -> cuisine == null || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(cuisine.toLowerCase())))
                .filter(r -> price == null || r.getPriceCategory() == price)
                .filter(r -> categories == null || categories.isEmpty() || r.getCategories().stream().anyMatch(categories::contains))
                .filter(r -> radiusKm == null || distanceKm(centerLat, centerLon, r.getLatitude(), r.getLongitude()) <= radiusKm)
                .filter(r -> visited == null || user != null && relationships.findByUserAndRestaurant(user, r)
                        .map(UserRestaurant::getVisited).orElse(false).equals(visited))
                .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER))
                .map(r -> toView(r, user))
                .toList();
    }

    @Transactional
    public RestaurantView getActive(Long id, User user) {
        return toView(restaurants.findByIdAndStatus(id, RestaurantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active restaurant not found")), user);
    }

    @Transactional
    public RestaurantDetailView getDetail(Long id, User user) {
        Restaurant restaurant = activeRestaurant(id);
        return new RestaurantDetailView(toView(restaurant, user),
                reviews.findByUserRestaurant_Restaurant_IdAndDeletedFalse(id).stream().map(this::reviewView).toList());
    }

    @Transactional
    public RelationshipView saveRelationship(Long restaurantId, RelationshipRequest request, User user) {
        Restaurant restaurant = activeRestaurant(restaurantId);
        UserRestaurant relation = relationships.findByUserAndRestaurant(user, restaurant).orElseGet(() -> {
            UserRestaurant created = new UserRestaurant();
            created.setUser(user);
            created.setRestaurant(restaurant);
            return created;
        });
        relation.setVisited(request.visited());
        return relationshipView(relationships.save(relation));
    }

    @Transactional
    public ReviewView saveReview(Long restaurantId, ReviewRequest request, User user) {
        Restaurant restaurant = activeRestaurant(restaurantId);
        if (request.overallRating() == null && request.valueForMoneyRating() == null
                && request.foodQualityRating() == null && request.ambienceRating() == null
                && (request.text() == null || request.text().isBlank())
                && (request.imageUrls() == null || request.imageUrls().isEmpty())) {
            throw new BadRequestException("Review must contain text, images, or a rating");
        }
        UserRestaurant relation = relationships.findByUserAndRestaurant(user, restaurant).orElseGet(() -> {
            UserRestaurant created = new UserRestaurant();
            created.setUser(user);
            created.setRestaurant(restaurant);
            created.setVisited(true);
            return relationships.save(created);
        });
        relation.setVisited(true);
        relationships.save(relation);
        Review review = reviews.findByUserRestaurant(relation).orElseGet(() -> {
            Review created = new Review();
            created.setUserRestaurant(relation);
            return created;
        });
        review.setOverallRating(request.overallRating());
        review.setValueForMoneyRating(request.valueForMoneyRating());
        review.setFoodQualityRating(request.foodQualityRating());
        review.setAmbienceRating(request.ambienceRating());
        review.setText(request.text());
        review.setImageUrls(request.imageUrls() == null ? List.of() : List.copyOf(request.imageUrls()));
        review.setDeleted(false);
        return reviewView(reviews.save(review));
    }

    @Transactional
    public void deleteReview(Long restaurantId, User user) {
        Restaurant restaurant = activeRestaurant(restaurantId);
        UserRestaurant relation = relationships.findByUserAndRestaurant(user, restaurant)
                .orElseThrow(() -> new NotFoundException("User restaurant relationship not found"));
        Review review = reviews.findByUserRestaurant(relation)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        review.setDeleted(true);
        reviews.save(review);
    }

    @Transactional
    public RestaurantEditView submitEdit(Long restaurantId, RestaurantEditRequest request, User user) {
        Restaurant restaurant = activeRestaurant(restaurantId);
        if (request.name() == null && request.address() == null && request.latitude() == null && request.longitude() == null
                && request.cuisine() == null && request.priceCategory() == null && request.categories() == null && request.googlePlaceId() == null) {
            throw new BadRequestException("At least one restaurant field must be proposed");
        }
        RestaurantEdit edit = new RestaurantEdit();
        edit.setRestaurant(restaurant);
        edit.setSubmittedBy(user);
        edit.setProposedName(blankToNull(request.name()));
        edit.setProposedAddress(blankToNull(request.address()));
        edit.setProposedLatitude(request.latitude());
        edit.setProposedLongitude(request.longitude());
        edit.setProposedCuisine(blankToNull(request.cuisine()));
        edit.setProposedPriceCategory(request.priceCategory());
        edit.setProposedCategories(request.categories() == null ? null : Set.copyOf(request.categories()));
        edit.setProposedGooglePlaceId(blankToNull(request.googlePlaceId()));
        edit.setStatus(RestaurantEditStatus.PENDING);
        return editView(edits.save(edit));
    }

    @Transactional
    public List<RestaurantView> pendingRestaurants() {
        return restaurants.findByStatus(RestaurantStatus.PENDING).stream().map(r -> toView(r, null)).toList();
    }

    @Transactional
    public RestaurantView moderateRestaurant(Long id, boolean approve, ModerationRequest request, User moderator) {
        Restaurant restaurant = restaurants.findById(id).orElseThrow(() -> new NotFoundException("Restaurant not found"));
        if (restaurant.getStatus() != RestaurantStatus.PENDING) throw new ConflictException("Restaurant is already moderated");
        restaurant.setStatus(approve ? RestaurantStatus.ACTIVE : RestaurantStatus.REJECTED);
        return toView(restaurants.save(restaurant), moderator);
    }

    @Transactional
    public List<RestaurantEditView> pendingEdits() {
        return edits.findByStatusOrderByCreatedAtAsc(RestaurantEditStatus.PENDING).stream().map(this::editView).toList();
    }

    @Transactional
    public RestaurantEditView moderateEdit(Long id, boolean approve, ModerationRequest request, User moderator) {
        RestaurantEdit edit = edits.findById(id).orElseThrow(() -> new NotFoundException("Restaurant edit not found"));
        if (edit.getStatus() != RestaurantEditStatus.PENDING) throw new ConflictException("Restaurant edit is already moderated");
        if (approve) {
            Restaurant r = edit.getRestaurant();
            if (edit.getProposedName() != null) r.setName(edit.getProposedName());
            if (edit.getProposedAddress() != null) r.setAddress(edit.getProposedAddress());
            if (edit.getProposedLatitude() != null) r.setLatitude(edit.getProposedLatitude());
            if (edit.getProposedLongitude() != null) r.setLongitude(edit.getProposedLongitude());
            if (edit.getProposedCuisine() != null) r.setCuisine(edit.getProposedCuisine());
            if (edit.getProposedPriceCategory() != null) r.setPriceCategory(edit.getProposedPriceCategory());
            if (edit.getProposedCategories() != null) r.setCategories(Set.copyOf(edit.getProposedCategories()));
            if (edit.getProposedGooglePlaceId() != null) r.setGooglePlaceId(edit.getProposedGooglePlaceId());
            restaurants.save(r);
        }
        edit.setStatus(approve ? RestaurantEditStatus.APPROVED : RestaurantEditStatus.REJECTED);
        edit.setReviewedBy(moderator);
        edit.setReviewedAt(java.time.Instant.now());
        edit.setModerationNote(request == null ? null : request.note());
        return editView(edits.save(edit));
    }

    @Transactional
    public MyDexView myDex(User user) {
        List<RestaurantView> visited = relationships.findByUserAndVisitedTrueAndRestaurant_Status(user, RestaurantStatus.ACTIVE)
                .stream().map(r -> toView(r.getRestaurant(), user)).toList();
        long total = restaurants.findByStatus(RestaurantStatus.ACTIVE).size();
        double progress = total == 0 ? 0 : visited.size() * 100.0 / total;
        return new MyDexView(visited.size(), total, progress, visited);
    }

    private Restaurant activeRestaurant(Long id) {
        return restaurants.findByIdAndStatus(id, RestaurantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active restaurant not found"));
    }

    private RestaurantView toView(Restaurant r, User user) {
        List<Review> restaurantReviews = reviews.findByUserRestaurant_Restaurant_IdAndDeletedFalse(r.getId());
        double average = restaurantReviews.stream().map(Review::getOverallRating).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0);
        RelationshipView relation = user == null ? null : relationships.findByUserAndRestaurant(user, r).map(this::relationshipView).orElse(null);
        return new RestaurantView(r.getId(), r.getName(), r.getAddress(), r.getLatitude(), r.getLongitude(), r.getCuisine(),
                r.getPriceCategory(), r.getGooglePlaceId(), r.getCategories(), r.getStatus(), relation, average, restaurantReviews.size());
    }

    private RelationshipView relationshipView(UserRestaurant relation) {
        return new RelationshipView(relation.getId(), Boolean.TRUE.equals(relation.getVisited()),
                reviews.findByUserRestaurant(relation).filter(review -> !Boolean.TRUE.equals(review.getDeleted())).map(this::reviewView).orElse(null));
    }

    private ReviewView reviewView(Review review) {
        User user = review.getUserRestaurant().getUser();
        return new ReviewView(review.getId(), user.getId(), user.getFullName(), review.getOverallRating(), review.getValueForMoneyRating(),
                review.getFoodQualityRating(), review.getAmbienceRating(), review.getText(), review.getImageUrls(), review.getCreatedAt(), review.getUpdatedAt());
    }

    private RestaurantEditView editView(RestaurantEdit edit) {
        RestaurantEditRequest proposed = new RestaurantEditRequest(edit.getProposedName(), edit.getProposedAddress(), edit.getProposedLatitude(),
                edit.getProposedLongitude(), edit.getProposedCuisine(), edit.getProposedPriceCategory(), edit.getProposedCategories(), edit.getProposedGooglePlaceId());
        return new RestaurantEditView(edit.getId(), edit.getRestaurant().getId(), edit.getSubmittedBy().getId(), edit.getStatus(), proposed,
                edit.getCreatedAt(), edit.getReviewedBy() == null ? null : edit.getReviewedBy().getId(), edit.getReviewedAt(), edit.getModerationNote());
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private static void validateCoordinate(double lat, double lon) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) throw new BadRequestException("Invalid coordinates");
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        validateCoordinate(lat1, lon1);
        validateCoordinate(lat2, lon2);
        double lat = Math.toRadians(lat2 - lat1);
        double lon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lon / 2) * Math.sin(lon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
