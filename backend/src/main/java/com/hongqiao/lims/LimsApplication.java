package com.hongqiao.lims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimsApplication.class, args);
    }
}
