package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.infrastructure.persistence.InMemorySessionRepository;

@Configuration
public class InfrastructureConfig {
    @Bean
    public SessionRepository sessionRepository() {
        return new InMemorySessionRepository();
    }
}
