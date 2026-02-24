package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.impl.*;
import ru.lifegame.backend.domain.service.GameEngine;
import ru.lifegame.backend.domain.service.ConflictTriggers;
import ru.lifegame.backend.domain.service.GameOverChecker;
import ru.lifegame.backend.domain.service.EndingEvaluator;

import java.util.List;

@Configuration
public class DomainConfig {

    @Bean
    public GoToWorkAction goToWorkAction() { return new GoToWorkAction(); }

    @Bean
    public VisitFatherAction visitFatherAction() { return new VisitFatherAction(); }

    @Bean
    public DateWithHusbandAction dateWithHusbandAction() { return new DateWithHusbandAction(); }

    @Bean
    public PlayWithCatAction playWithCatAction() { return new PlayWithCatAction(); }

    @Bean
    public WalkDogAction walkDogAction() { return new WalkDogAction(); }

    @Bean
    public SelfCareAction selfCareAction() { return new SelfCareAction(); }

    @Bean
    public RestAtHomeAction restAtHomeAction() { return new RestAtHomeAction(); }

    @Bean
    public GameEngine gameEngine(List<GameAction> actions) {
        return new GameEngine(actions, conflictTriggers(), gameOverChecker());
    }

    @Bean
    public ConflictTriggers conflictTriggers() { return new ConflictTriggers(); }

    @Bean
    public GameOverChecker gameOverChecker() { return new GameOverChecker(); }

    @Bean
    public EndingEvaluator endingEvaluator() { return new EndingEvaluator(); }
}
