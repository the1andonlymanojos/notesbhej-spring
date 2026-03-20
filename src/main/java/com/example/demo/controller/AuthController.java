package com.example.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
