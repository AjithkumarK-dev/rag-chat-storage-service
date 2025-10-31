package com.ragchat.chatservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragchat.chatservice.dto.ApiResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter based on API keys loaded from .env
 * Each key has its own limit and time window.
 * Safe with Swagger, ApiKeyFilter, and SecurityConfig.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Environment env;

    private final Map<String, RequestBucket> rateLimiters = new ConcurrentHashMap<>();

    public RateLimitFilter(Environment env) {
        this.env = env;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip Swagger, actuator, and static resources
        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator") || path.endsWith(".js") || path.endsWith(".css")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read API key from header
        String apiKey = request.getHeader("x-api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            // ApiKeyFilter will handle 401s, do not block here
            filterChain.doFilter(request, response);
            return;
        }

        // Fetch per-key limit and window from .env (case-insensitive)
        int limit = Integer.parseInt(env.getProperty("RATE_LIMIT_" + apiKey.toUpperCase(),
                env.getProperty("RATE_LIMIT_DEFAULT", "5")));

        long windowMs = Long.parseLong(env.getProperty("RATE_WINDOW_" + apiKey.toUpperCase(),
                env.getProperty("RATE_WINDOW_DEFAULT", "60000")));

        // Track requests per API key
        RequestBucket bucket = rateLimiters.computeIfAbsent(apiKey, k -> new RequestBucket(windowMs, limit));

        synchronized (bucket) {
            long now = Instant.now().toEpochMilli();

            // Reset if window expired
            if (now - bucket.windowStart >= bucket.windowMs) {
                bucket.windowStart = now;
                bucket.requestCount = 0;
            }

            // Check limit
            if (bucket.requestCount >= bucket.limit) {
                long wait = bucket.windowMs - (now - bucket.windowStart);
                sendError(response, 429,
                        "Rate limit exceeded for API key '" + apiKey +
                                "'. Try again in " + (wait / 1000) + " seconds.");
                return;
            }

            bucket.requestCount++;

            // (Optional logging, won’t affect anything)
            log.debug("API Key [{}] → {}/{} requests (window={}ms)", apiKey, bucket.requestCount, bucket.limit, bucket.windowMs);
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        ApiResponseDTO error = new ApiResponseDTO(code, message, null);
        response.getWriter().write(mapper.writeValueAsString(error));
    }

    private static class RequestBucket {
        long windowStart = Instant.now().toEpochMilli();
        int requestCount = 0;
        final long windowMs;
        final int limit;

        RequestBucket(long windowMs, int limit) {
            this.windowMs = windowMs;
            this.limit = limit;
        }
    }
}