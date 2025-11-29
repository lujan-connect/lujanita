package com.lujanita.bff.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {
    private static final int LIMIT = 60;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, Window> apiKeyWindows = new ConcurrentHashMap<>();

    @Autowired
    private BffProperties bffProperties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // SDD: Filtro de rate limiting por apiKey, responde MW007 si se excede el límite
        if (bffProperties.getRateLimit() != null && !bffProperties.getRateLimit().isEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        int limit = bffProperties.getRateLimit() != null ? bffProperties.getRateLimit().getRequestsPerMinute() : LIMIT;
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            String apiKey = req.getHeader("X-Api-Key");
            String correlationId = req.getHeader("X-Correlation-Id");
            if (apiKey != null) {
                Window window = apiKeyWindows.computeIfAbsent(apiKey, k -> new Window());
                synchronized (window) {
                    long now = System.currentTimeMillis();
                    if (now - window.start >= WINDOW_MS) {
                        window.start = now;
                        window.count.set(0);
                    }
                    if (window.count.incrementAndGet() > limit) {
                        resp.setStatus(429);
                        resp.setContentType("application/json");
                        String body = String.format("{\"code\":\"MW007\",\"message\":\"Rate limit exceeded\",\"correlationId\":\"%s\"}", correlationId != null ? correlationId : "");
                        resp.getWriter().write(body);
                        // Logging estructurado (puede integrarse con observabilidad)
                        System.out.printf("[BFF][RateLimit] apiKey=%s correlationId=%s MW007\n", apiKey, correlationId);
                        return;
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static class Window {
        long start = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
