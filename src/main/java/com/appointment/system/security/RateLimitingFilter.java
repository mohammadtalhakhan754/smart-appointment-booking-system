package com.appointment.system.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {

    @Autowired
    Supplier<BucketConfiguration> bucketConfiguration;

    @Autowired
    ProxyManager<String> proxyManager;

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // Skip rate limiting for excluded paths
        if (isExcludedPath(httpRequest.getRequestURI())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Use client's remote address as bucket key
        String key = httpRequest.getRemoteAddr();

        // Get or create bucket for this client
        Bucket bucket = proxyManager.builder().build(key, bucketConfiguration);

        // Try to consume 1 token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        log.debug(">>>>>>>>remainingTokens: {}", probe.getRemainingTokens());

        if (probe.isConsumed()) {
            // Request allowed - proceed
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // Rate limit exceeded - return 429
            httpResponse.setContentType("text/plain");
            httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())));
            httpResponse.setStatus(429);
            httpResponse.getWriter().append("Too many requests");
        }
    }

    /**
     * Check if path should be excluded from rate limiting
     */
    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/actuator/health");
    }
}
