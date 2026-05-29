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
