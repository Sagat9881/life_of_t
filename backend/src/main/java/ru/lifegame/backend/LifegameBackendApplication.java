package ru.lifegame.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LifegameBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifegameBackendApplication.class, args);
    }
}
