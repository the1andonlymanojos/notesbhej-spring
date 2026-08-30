package com.example.demo.controller;

import com.example.demo.config.AuthCookies;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthCookies authCookies;

    public AuthController(AuthCookies authCookies) {
        this.authCookies = authCookies;
    }

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
        authCookies.clear(response);
    }
}
