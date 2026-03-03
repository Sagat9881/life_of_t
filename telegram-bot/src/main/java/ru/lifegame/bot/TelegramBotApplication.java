package ru.lifegame.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import ru.lifegame.bot.config.BotProperties;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.lifegame.bot", "ru.lifegame.backend"})
@EnableConfigurationProperties(BotProperties.class)
public class TelegramBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
    }
}
