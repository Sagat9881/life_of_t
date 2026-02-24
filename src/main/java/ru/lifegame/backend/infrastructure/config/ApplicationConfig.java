package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.application.port.in.*;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.service.*;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.service.GameEngine;

import java.util.List;

@Configuration
public class ApplicationConfig {

    @Bean
    public GameStateViewMapper gameStateViewMapper(List<GameAction> allActions) {
        return new GameStateViewMapper(allActions);
    }

    @Bean
    public StartOrLoadSessionUseCase startOrLoadSessionUseCase(SessionRepository repo,
                                                                GameStateViewMapper mapper) {
        return new StartOrLoadSessionService(repo, mapper);
    }

    @Bean
    public ExecutePlayerActionUseCase executePlayerActionUseCase(SessionRepository repo,
                                                                  GameEngine engine,
                                                                  EventPublisher publisher,
                                                                  GameStateViewMapper mapper) {
        return new ExecutePlayerActionService(repo, engine, publisher, mapper);
    }

    @Bean
    public GetGameStateUseCase getGameStateUseCase(SessionRepository repo, GameStateViewMapper mapper) {
        return new GetGameStateService(repo, mapper);
    }
}
