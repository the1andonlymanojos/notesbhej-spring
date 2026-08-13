package com.example.demo;

import com.example.demo.dto.khaao.KhaaoDexDtos.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.KhaaoDexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KhaaoDexServiceTest {
    @Mock RestaurantRepository restaurants;
    @Mock UserRestaurantRepository relationships;
    @Mock ReviewRepository reviews;
    @Mock RestaurantEditRepository edits;

    private KhaaoDexService service;
    private User user;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        service = new KhaaoDexService(restaurants, relationships, reviews, edits, 26.2025, 78.1746);
        user = new User();
        user.setId(10L);
        user.setFullName("Tester");
        restaurant = restaurant(1L, "Near", 26.2025, 78.1746, RestaurantStatus.ACTIVE);
    }

    @Test
    void submissionAlwaysStartsPending() {
        when(restaurants.save(any())).thenAnswer(invocation -> {
            Restaurant saved = invocation.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        RestaurantView result = service.submit(new RestaurantSubmissionRequest(
                " New Place ", "Address", 26.2, 78.17, "Indian", RestaurantPriceCategory.BUDGET, "place-1"), user);

        assertEquals(RestaurantStatus.PENDING, result.status());
        verify(restaurants).save(argThat(r -> r.getStatus() == RestaurantStatus.PENDING && r.getCreatedBy() == user));
    }

    @Test
    void discoveryReturnsOnlyActiveRestaurantsWithinRadius() {
        Restaurant far = restaurant(2L, "Far", 27.5, 79.5, RestaurantStatus.ACTIVE);
        Restaurant pending = restaurant(3L, "Pending", 26.2, 78.17, RestaurantStatus.PENDING);
        when(restaurants.findByStatus(RestaurantStatus.ACTIVE)).thenReturn(List.of(restaurant, far));
        when(reviews.findByUserRestaurant_Restaurant_IdAndDeletedFalse(any())).thenReturn(List.of());

        List<RestaurantView> result = service.discover(null, null, null, 26.2025, 78.1746, 1.0, null);

        assertEquals(List.of("Near"), result.stream().map(RestaurantView::name).toList());
        assertNotEquals(RestaurantStatus.PENDING, result.get(0).status());
        verify(restaurants).findByStatus(RestaurantStatus.ACTIVE);
    }

    @Test
    void relationshipCanBeVisitedWithoutReview() {
        when(restaurants.findByIdAndStatus(1L, RestaurantStatus.ACTIVE)).thenReturn(Optional.of(restaurant));
        UserRestaurant mapping = mapping(user, restaurant);
        when(relationships.findByUserAndRestaurant(user, restaurant)).thenReturn(Optional.of(mapping));
        when(relationships.save(mapping)).thenReturn(mapping);
        when(reviews.findByUserRestaurant(mapping)).thenReturn(Optional.empty());

        RelationshipView result = service.saveRelationship(1L, new RelationshipRequest(true), user);

        assertTrue(result.visited());
        assertNull(result.review());
        verify(reviews, never()).save(any());
    }

    @Test
    void reviewCreatesRelationshipAndCanContainOnlyText() {
        when(restaurants.findByIdAndStatus(1L, RestaurantStatus.ACTIVE)).thenReturn(Optional.of(restaurant));
        when(relationships.findByUserAndRestaurant(user, restaurant)).thenReturn(Optional.empty());
        when(relationships.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviews.findByUserRestaurant(any())).thenReturn(Optional.empty());
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewView result = service.saveReview(1L,
                new ReviewRequest(null, null, null, null, "Good food", List.of()), user);

        assertEquals("Good food", result.text());
        verify(relationships, times(2)).save(any());
        verify(reviews).save(any());
    }

    @Test
    void approvedEditChangesCanonicalRestaurant() {
        RestaurantEdit edit = new RestaurantEdit();
        edit.setId(8L);
        edit.setRestaurant(restaurant);
        edit.setSubmittedBy(user);
        edit.setStatus(RestaurantEditStatus.PENDING);
        edit.setProposedName("Updated Name");
        when(edits.findById(8L)).thenReturn(Optional.of(edit));
        when(edits.save(edit)).thenReturn(edit);

        service.moderateEdit(8L, true, new ModerationRequest("looks right"), user);

        assertEquals("Updated Name", restaurant.getName());
        assertEquals(RestaurantEditStatus.APPROVED, edit.getStatus());
        verify(restaurants).save(restaurant);
    }

    private static Restaurant restaurant(Long id, String name, double lat, double lon, RestaurantStatus status) {
        Restaurant r = new Restaurant();
        r.setId(id);
        r.setName(name);
        r.setAddress("Address");
        r.setLatitude(lat);
        r.setLongitude(lon);
        r.setStatus(status);
        return r;
    }

    private static UserRestaurant mapping(User user, Restaurant restaurant) {
        UserRestaurant mapping = new UserRestaurant();
        mapping.setId(20L);
        mapping.setUser(user);
        mapping.setRestaurant(restaurant);
        mapping.setVisited(false);
        return mapping;
    }
}
