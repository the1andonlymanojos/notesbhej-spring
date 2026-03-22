package com.example.demo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(-1000) // keep it early
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // reuse incoming CID if present
        String cid = request.getHeader(HEADER);
        if (cid == null || cid.isEmpty()) {
            cid = UUID.randomUUID().toString();
        }

        // extract real IP (important behind proxy)
        String ip = request.getHeader("CF-Connecting-IP");

        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty()) {
                ip = ip.split(",")[0];
            }
        }

        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }


        MDC.put("cid", cid);
        MDC.put("ip", ip);

        response.setHeader(HEADER, cid);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}


