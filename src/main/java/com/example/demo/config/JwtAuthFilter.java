package com.example.demo.config;

import com.example.demo.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }

            }
        }
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                User user = jwtService.validate(token);

                if (user != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    // 👇 Ensure demo_id cookie exists
                    boolean hasDemoId = false;

                    for (Cookie cookie : cookies) {
                        if ("demo_id".equals(cookie.getName())) {
                            hasDemoId = true;
                            break;
                        }
                    }
                    if (!hasDemoId) {
                        Cookie demoCookie = getDemoCookie(user);

                        response.addCookie(demoCookie);
                    }



                } else {
                    clearCookie(response);
                }

            } catch (Exception e) {
                clearCookie(response);
            }
        }
        filterChain.doFilter(request, response);
    }

    private static @NonNull Cookie getDemoCookie(User user) {
        Cookie demoCookie = new Cookie("demo_id", user.getUserId().toString());
        demoCookie.setPath("/");
        demoCookie.setHttpOnly(false); // allow JS if needed for demo
        demoCookie.setSecure(false);   // set true in production (HTTPS)
        demoCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        //demoCookie.setDomain(".mshiv.net");
        return demoCookie;
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // keep consistent with how you set it
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete immediately
        response.addCookie(cookie);
    }
}