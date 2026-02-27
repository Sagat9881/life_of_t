package ru.lifegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Life of T.
 * Запускает Spring Boot приложение с backend API и frontend статикой.
 */
@SpringBootApplication(scanBasePackages = "ru.lifegame")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
