package ru.lifegame.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Telegram-бота.
 * Значения берутся из переменных окружения или application.yml.
 */
@Configuration
public class BotConfig {

    @Value("${bot.token:your-bot-token-here}")
    private String token;

    @Value("${bot.username:LifeOfTBot}")
    private String username;

    @Value("${bot.webapp-url:https://your-domain.com}")
    private String webappUrl;

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getWebappUrl() { return webappUrl; }
}
