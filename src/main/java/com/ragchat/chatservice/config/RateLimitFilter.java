package com.ragchat.chatservice.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long TIME_WINDOW_MS = 60 * 1000;

    private final Map<String, UserRateLimit> requestCounts = new ConcurrentHashMap<>();
    private static final int TOO_MANY_REQUESTS_STATUS = resolveTooManyRequestsStatus();

    private static int resolveTooManyRequestsStatus() {
        try {
            Field field = HttpServletResponse.class.getField("SC_TOO_MANY_REQUESTS");
            return (int) field.get(null);
        } catch (Exception e) {
            // Fallback to literal 429 for older servlet APIs
            return 429;
        }
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = request.getRemoteAddr();



        UserRateLimit rateLimit = requestCounts.computeIfAbsent(clientIp, k -> new UserRateLimit());

        synchronized (rateLimit) {
            long now = Instant.now().toEpochMilli();
            if (now - rateLimit.timestamp > TIME_WINDOW_MS) {
                rateLimit.timestamp = now;
                rateLimit.count.set(0);
            }

            if (rateLimit.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP {}", clientIp);
                httpResponse.setStatus(TOO_MANY_REQUESTS_STATUS);
                httpResponse.setContentType("application/json");
                httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                httpResponse.setHeader("X-RateLimit-Remaining", "0");
                httpResponse.getWriter().write("""
                    {
                      "code": 429,
                      "message": "Too Many Requests — Rate limit exceeded"
                    }
                """);
                return;
            } else {
                int remaining = MAX_REQUESTS_PER_MINUTE - rateLimit.count.get();
                httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            }
        }

        chain.doFilter(request, response);
    }

    private static class UserRateLimit {
        long timestamp = Instant.now().toEpochMilli();
        AtomicInteger count = new AtomicInteger(0);
    }
}
