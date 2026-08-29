package com.example.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /** Must match OAuthSuccessHandler so logout clears the same cookie. */
    @Value("${APP_COOKIE_DOMAIN:}")
    private String cookieDomain;

    @GetMapping("/google")
    public void google(
            @RequestParam(required = false) String redirect,
            HttpServletResponse response
    ) throws IOException {

        if (redirect != null) {
            Cookie cookie = new Cookie("oauth_redirect", redirect);
            cookie.setPath("/");
            cookie.setMaxAge(300);
            response.addCookie(cookie);
        }

        response.sendRedirect("/oauth2/authorization/google");
    }


    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {

        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None");
        if (cookieDomain != null && !cookieDomain.isBlank()) cb.domain(cookieDomain);

        response.addHeader(HttpHeaders.SET_COOKIE, cb.build().toString());
    }
}
