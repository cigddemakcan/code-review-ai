package com.example.codereviewai.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.greedy(5, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createBucket());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !path.startsWith("/api/github/review")
                && !path.startsWith("/api/reviews");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String path = request.getRequestURI();

        Bucket bucket = getBucket(clientIp);

        if (bucket.tryConsume(1)) {
            log.debug("Rate limit check passed. ip={}, path={}", clientIp, path);
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rate limit exceeded. ip={}, path={}", clientIp, path);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
                {
                  "status": 429,
                  "message": "Too many AI review requests. Please try again later."
                }
                """);
    }
}