package com.example.demo.config;

import com.example.demo.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
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

        System.out.println("---- incoming request ----");
        System.out.println("path = " + request.getRequestURI());

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            System.out.println("no cookies");
        } else {
            for (Cookie c : cookies) {
                System.out.println(c.getName() + " = " + c.getValue());
            }
        }
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName());

                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }

            }
        }
        System.out.println("tok"+token);
        if (token != null) {

            User user = jwtService.validate(token);

            System.out.println("validated user = " + user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}