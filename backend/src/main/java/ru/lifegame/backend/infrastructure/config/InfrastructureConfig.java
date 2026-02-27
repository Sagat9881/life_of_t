package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.infrastructure.event.LoggingEventPublisher;

@Configuration
public class InfrastructureConfig {

    @Bean
    public EventPublisher eventPublisher() {
        return new LoggingEventPublisher();
    }
}
