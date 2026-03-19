package com.navtex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class NavtexApplication {
    public static void main(String[] args) {
        SpringApplication.run(NavtexApplication.class, args);
    }
}
