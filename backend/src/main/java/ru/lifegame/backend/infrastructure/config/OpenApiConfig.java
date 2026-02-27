package ru.lifegame.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Игра Life of T - REST API")
                        .version("1.0.0")
                        .description("""
                            REST API для игры Life of T - симулятор жизни главной героини.
                            
                            **Основные возможности:**
                            - Управление игровой сессией
                            - Выполнение действий (работа, свидания, уход за питомцами)
                            - Разрешение конфликтов
                            - Выбор вариантов в событиях
                            - Отслеживание состояния персонажа, отношений и прогресса квестов
                            
                            **Архитектура:**
                            - Clean Architecture
                            - Domain-Driven Design
                            - Event Sourcing
                            """)
                        .contact(new Contact()
                                .name("Команда разработки Life of T")
                                .email("support@lifeoft.game"))
                        .license(new License()
                                .name("Проприетарная лицензия")
                                .url("https://lifeoft.game/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер разработки"),
                        new Server()
                                .url("https://api.lifeoft.game")
                                .description("Продакшен сервер")
                ));
    }
}
