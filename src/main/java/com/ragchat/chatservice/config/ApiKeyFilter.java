package com.ragchat.chatservice.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiKeyFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    // Default to an empty list to prevent NullPointerException
    private List<String> keys = Collections.emptyList();

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    @PostConstruct
    public void init() {
        if (keys == null || keys.isEmpty()) {
            log.warn("No API keys configured — API key validation will be skipped (development mode).");
        } else {
            log.info("Loaded {} API key(s) for authentication", keys.size());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Skip security for public endpoints
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/health")) {
            chain.doFilter(request, response);
            return;
        }

        // Skip key validation entirely if no keys are configured
        if (keys == null || keys.isEmpty()) {
            log.debug("Skipping API key check — no keys configured (dev mode).");
            chain.doFilter(request, response);
            return;
        }

        String requestApiKey = httpRequest.getHeader("X-API-KEY");

        if (requestApiKey == null || !keys.contains(requestApiKey)) {
            log.warn("Unauthorized access to {} from IP {}", path, request.getRemoteAddr());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("""
                {
                  "code": 401,
                  "message": "Unauthorized — Invalid or missing API key"
                }
            """);
            return;
        }

        chain.doFilter(request, response);
    }
}
