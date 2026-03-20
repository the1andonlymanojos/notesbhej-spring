package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;


@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    public OAuthSuccessHandler(JwtService jwtService, UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");

        User user = userRepo.findByGoogleId(googleId)
                .orElseGet(() -> {

                    // check if this email already exists (old Supabase user)
                    Optional<User> existing = userRepo.findByEmail(email);

                    if (existing.isPresent()) {
                        User u = existing.get();

                        // link Google account
                        u.setGoogleId(googleId);

                        return userRepo.save(u);
                    }

                    // brand new user
                    return userRepo.save(User.fromGoogle(oauthUser));
                });

        String token = jwtService.generateToken(user);

        System.out.println("token"+token);
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true later
        cookie.setPath("/");
        cookie.setMaxAge(3600);

        String redirect = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("oauth_redirect".equals(c.getName())) {
                    redirect = c.getValue();
                }
            }
        }
        System.out.println("redirect param = " + redirect);

        String frontend = "http://localhost:3000";

        if (redirect == null) {
            redirect = frontend;
        }
        else if (redirect.startsWith("/")) {
            redirect = frontend + redirect;
        }
        else if (!redirect.startsWith("http")) {
            redirect = frontend;
        }

        request.getSession().removeAttribute("OAUTH_REDIRECT");

        response.addCookie(cookie);
        response.sendRedirect(redirect);


    }


}