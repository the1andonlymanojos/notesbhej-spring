package com.example.demo.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * One place that knows how the {@code access_token} cookie is shaped, so login,
 * logout and the auth filter stay in sync.
 *
 * <p>{@code APP_COOKIE_DOMAIN} = {@code .mshiv.net} shares the session across every
 * *.mshiv.net project; empty keeps it host-only.
 */
@Component
public class AuthCookies {

    public static final String NAME = "access_token";

    @Value("${APP_COOKIE_DOMAIN:}")
    private String domain;

    private boolean hasDomain() {
        return domain != null && !domain.isBlank();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(NAME, value)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None");
        if (hasDomain()) b.domain(domain);
        return b;
    }

    /** Set the session cookie after a successful login. */
    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, base(token).maxAge(Duration.ofDays(30)).build().toString());
    }

    /**
     * Delete the session cookie. Emits both the domain-scoped cookie and a
     * host-only one so a stale pre-migration cookie is cleared too.
     */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, base("").maxAge(0).build().toString());
        if (hasDomain()) {
            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    ResponseCookie.from(NAME, "")
                            .path("/")
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("None")
                            .maxAge(0)
                            .build()
                            .toString());
        }
    }
}
