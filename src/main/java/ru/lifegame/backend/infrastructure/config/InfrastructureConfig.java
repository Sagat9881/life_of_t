package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class InfrastructureConfig {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureConfig.class);

    @Bean
    public EventPublisher eventPublisher() {
        return event -> log.info("Domain event published: {}", event.getClass().getSimpleName());
    }
}
