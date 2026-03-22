package com.example.demo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Order(102)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();

            String ip = MDC.get("ip");     // from your earlier filter
            String user = MDC.get("user"); // from auth filter

            log.info("request",
                    kv("method", method),
                    kv("path", path),
                    kv("status", status),
                    kv("duration_ms", duration),
                    kv("ip", ip),
                    kv("user", user)
            );
        }
    }
}


