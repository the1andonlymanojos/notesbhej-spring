package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users_app")
@Setter
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id")
    private UUID userId;

    @Column(name="google_id")
    private String googleId;

    private String email;

    private String fullName;

    private String batch;

    private String profilePictureUrl;

    private Boolean adminRequest;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String bgPref;

    private Instant createdAt;

    private Instant updatedAt;



    public static User fromGoogle(OAuth2User google) {

        User user = new User();

        user.setGoogleId(google.getAttribute("sub"));
        user.setEmail(google.getAttribute("email"));
        user.setFullName(google.getAttribute("name"));
        user.setProfilePictureUrl(google.getAttribute("picture"));

        user.setRole(UserRole.STUDENT);

        user.setUserId(UUID.randomUUID());

        user.setCreatedAt(Instant.now());

        return user;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}