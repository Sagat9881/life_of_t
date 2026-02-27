package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.application.port.in.*;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.service.*;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

@Configuration
public class ApplicationConfig {

    @Bean
    public GameStateViewMapper gameStateViewMapper(ActionProvider actionProvider) {
        return new GameStateViewMapper(actionProvider.allActions().stream().toList());
    }

    @Bean
    public StartOrLoadSessionUseCase startOrLoadSessionUseCase(SessionRepository repo,
                                                                GameStateViewMapper mapper) {
        return new StartOrLoadSessionService(repo, mapper);
    }

    @Bean
    public ExecutePlayerActionUseCase executePlayerActionUseCase(SessionRepository repo,
                                                                  ActionProvider actionProvider,
                                                                  GameStateViewMapper mapper) {
        return new ExecutePlayerActionService(repo, actionProvider.allActions(), mapper);
    }

    @Bean
    public GetGameStateUseCase getGameStateUseCase(SessionRepository repo, GameStateViewMapper mapper) {
        return new GetGameStateService(repo, mapper);
    }

    @Bean
    public ChooseConflictTacticUseCase chooseConflictTacticUseCase(SessionRepository repo,
                                                                     EventPublisher publisher,
                                                                     GameStateViewMapper mapper) {
        return new ChooseConflictTacticService(repo, publisher, mapper);
    }

    @Bean
    public ChooseEventOptionUseCase chooseEventOptionUseCase(SessionRepository repo,
                                                               GameStateViewMapper mapper) {
        return new ChooseEventOptionService(repo,  mapper);
    }
}
