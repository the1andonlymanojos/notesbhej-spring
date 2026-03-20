package com.example.demo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuthRedirectCaptureFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        String redirect = request.getParameter("redirect");

        if (redirect != null) {
            request.getSession().setAttribute("OAUTH_REDIRECT", redirect);
        }

        chain.doFilter(req, res);
    }
}
