package com.kyotocuisine.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter using a sliding token-bucket per IP.
 *
 * Two buckets:
 *   - auth endpoints (/api/auth/login, /api/auth/register):  5 requests / minute
 *   - everything else:                                       60 requests / minute
 *
 * This stops brute-force password attacks and crude HTTP flooding without
 * any external library. Buckets live in a ConcurrentHashMap keyed by IP.
 */
public class RateLimitFilter implements Filter {

    private static final long WINDOW_MILLIS = 60_000L; // 1 minute
    private static final int AUTH_LIMIT = 5;
    private static final int GLOBAL_LIMIT = 60;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = request.getRequestURI();
        // Only rate-limit API calls. Static files (HTML, CSS, JS, images) bypass the filter.
        if (!path.startsWith("/api/")) {
            chain.doFilter(req, resp);
            return;
        }

        String ip = clientIp(request);
        boolean isAuth = path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");
        int limit = isAuth ? AUTH_LIMIT : GLOBAL_LIMIT;
        String key = ip + (isAuth ? "|auth" : "|global");

        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        if (!bucket.allow(limit)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Too many requests. Slow down and try again in a minute.\"}"
            );
            return;
        }
        chain.doFilter(req, resp);
    }

    /** Get the real client IP, honoring common proxy headers. */
    private String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isEmpty()) {
            // first IP in the chain is the originating client
            int comma = h.indexOf(',');
            return comma > 0 ? h.substring(0, comma).trim() : h.trim();
        }
        h = req.getHeader("X-Real-IP");
        if (h != null && !h.isEmpty()) return h;
        return req.getRemoteAddr();
    }

    /** A simple sliding-window bucket: track timestamps of requests in the last minute. */
    private static class Bucket {
        private final long[] slots = new long[128]; // max requests we can "remember"
        private int head = 0;

        synchronized boolean allow(int limit) {
            long now = System.currentTimeMillis();
            long cutoff = now - WINDOW_MILLIS;

            // Count how many slots are still within the window
            int count = 0;
            for (long t : slots) if (t > cutoff) count++;

            if (count >= limit) return false;

            slots[head] = now;
            head = (head + 1) % slots.length;
            return true;
        }
    }
}
