package com.example.demo.config;

import com.example.demo.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthCookies authCookies;

    public JwtAuthFilter(JwtService jwtService, AuthCookies authCookies) {
        this.jwtService = jwtService;
        this.authCookies = authCookies;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        if (cookies != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean sawToken = false;
            User user = null;

            // A domain migration can leave a host-only and a domain-scoped
            // access_token side by side. Try each; the first that validates wins.
            for (Cookie cookie : cookies) {
                if (!AuthCookies.NAME.equals(cookie.getName())) continue;
                sawToken = true;
                String value = cookie.getValue();
                if (value == null || value.isBlank()) continue;
                try {
                    User candidate = jwtService.validate(value);
                    if (candidate != null) {
                        user = candidate;
                        break;
                    }
                } catch (Exception ignored) {
                    // try the next cookie
                }
            }

            if (user != null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                boolean hasDemoId = false;
                for (Cookie cookie : cookies) {
                    if ("demo_id".equals(cookie.getName())) {
                        hasDemoId = true;
                        break;
                    }
                }
                if (!hasDemoId) {
                    response.addCookie(getDemoCookie(user));
                }
            } else if (sawToken) {
                // Had an access_token cookie but nothing validated — clear it.
                authCookies.clear(response);
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
}
