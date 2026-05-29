package com.hongqiao.lims.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "lims")
public class LimsProperties {

    private String apiPrefix = "/api/v1";
    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Admin admin = new Admin();
    private final Security security = new Security();

    @Getter
    @Setter
    public static class Security {
        /** Minimum password length enforced on create / change-password. */
        private int passwordMinLength = 8;
        /** Failed login attempts before the account is temporarily locked. */
        private int maxFailedAttempts = 5;
        /** How long an account stays locked after exceeding the failed-attempt threshold. */
        private int lockoutMinutes = 15;
        /** Max login attempts per client IP within one minute (<= 0 disables rate limiting). */
        private int loginRateLimitPerMinute = 60;
        /** Abort startup if the JWT secret still looks like a placeholder (enable in prod). */
        private boolean enforceStrongSecret = false;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expireMinutes = 1440;
    }

    @Getter
    @Setter
    public static class Cors {
        private String origins = "http://localhost:5173,http://localhost:3000";
    }

    @Getter
    @Setter
    public static class Admin {
        private String username = "admin";
        private String password = "admin123";
        private String name = "系统管理员";
    }
}
