package com.gastapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GastappWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GastappWebApplication.class, args);
    }
}
