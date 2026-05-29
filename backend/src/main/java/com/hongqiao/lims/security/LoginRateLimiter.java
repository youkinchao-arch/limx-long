package com.hongqiao.lims.security;

import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.config.LimsProperties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory fixed-window rate limiter for the login endpoint, keyed by client IP.
 * Single-instance scope; behind a load balancer use a shared store (documented for ops).
 */
@Component
public class LoginRateLimiter {

    private final int limitPerMinute;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public LoginRateLimiter(LimsProperties props) {
        this.limitPerMinute = props.getSecurity().getLoginRateLimitPerMinute();
    }

    public void check(String clientIp) {
        if (limitPerMinute <= 0) {
            return;
        }
        long currentWindow = System.currentTimeMillis() / 60_000L;
        String key = clientIp == null ? "unknown" : clientIp;
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.minute != currentWindow) {
                return new Window(currentWindow);
            }
            return existing;
        });
        if (window.count.incrementAndGet() > limitPerMinute) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "登录尝试过于频繁，请稍后再试 / Too many login attempts, please try again later");
        }
    }

    private static final class Window {
        private final long minute;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long minute) {
            this.minute = minute;
        }
    }
}
