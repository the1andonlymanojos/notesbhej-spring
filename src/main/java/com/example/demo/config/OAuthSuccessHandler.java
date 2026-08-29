package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;


@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Value("${APP_FRONTEND_URL:https://notesbhej.mshiv.net}")
    private String frontend;

    /** Set to ".mshiv.net" to share the session across every *.mshiv.net project. Empty = host-only. */
    @Value("${APP_COOKIE_DOMAIN:}")
    private String cookieDomain;

    public OAuthSuccessHandler(JwtService jwtService, UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");
        String hd = oauthUser.getAttribute("hd");
        System.out.println("HD"+ hd);
        System.out.println("email: "+email);
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

                    // brand-new user
                    if (hd == null || !hd.equals("iiitm.ac.in")) {
                        System.out.println("USER IS NOT ALLOWED TO CREATE ACCT");
                        try {
                            response.sendRedirect(frontend+"/nextlogin?error=unauthorized_domain");
                        } catch (IOException e) {
                            System.out.println("EXCEPTION THROWN");
                            throw new RuntimeException(e);
                        }
                        System.out.println("RETURNING NULL");

                        return null; // stop further processing
                    }

                    // brand-new user
                    return userRepo.save(User.fromGoogle(oauthUser));
                });

        if (user==null){
            return;
        }
        String token = jwtService.generateToken(user);

        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("None");
        if (cookieDomain != null && !cookieDomain.isBlank()) cb.domain(cookieDomain);
        ResponseCookie cookie = cb.build();

        String redirect = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("oauth_redirect".equals(c.getName())) {
                    redirect = c.getValue();
                }
            }
        }
        System.out.println("redirect param = " + redirect);

        if (redirect == null) {
            redirect = frontend;
        } else if (redirect.startsWith("/")) {
            redirect = frontend + redirect;
        } else if (!isAllowedRedirect(redirect)) {
            // Open-redirect guard: only bounce back to our own domains.
            redirect = frontend;
        }

        request.getSession().removeAttribute("OAUTH_REDIRECT");

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect(redirect);


    }

    /** Allow only https redirects to mshiv.net and its subdomains. */
    private static boolean isAllowedRedirect(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && (host.equals("mshiv.net") || host.endsWith(".mshiv.net"));
        } catch (RuntimeException e) {
            return false;
        }
    }


}
