package ru.lifegame.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable record-конфигурация бота, привязанная к префиксу "bot".
 * Все поля берутся из application.yml или переменных окружения.
 *
 * @param token      токен бота от BotFather
 * @param username   @username бота без символа @
 * @param webappUrl  публичный URL веб-приложения (Telegram Mini App)
 */
@ConfigurationProperties(prefix = "bot")
public record BotProperties(
        String token,
        String username,
        String webappUrl
) {
}
