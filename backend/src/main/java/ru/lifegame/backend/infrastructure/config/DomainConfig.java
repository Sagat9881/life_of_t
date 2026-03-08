package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.spec.DataDrivenAction;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpec;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.model.session.GameOverChecker;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.List;

@Configuration
public class DomainConfig {

    @Bean
    public PlayerActionSpecLoader playerActionSpecLoader() {
        return new PlayerActionSpecLoader();
    }

    @Bean
    public List<GameAction> gameActions(PlayerActionSpecLoader specLoader) {
        List<PlayerActionSpec> specs = specLoader.loadAll();
        return specs.stream()
                .map(spec -> (GameAction) new DataDrivenAction(spec))
                .toList();
    }

    @Bean
    public ActionExecutor actionExecutor() {
        return new ActionExecutor();
    }

    @Bean
    public ConflictTriggers conflictTriggers() {
        return new ConflictTriggers();
    }

    @Bean
    public GameOverChecker gameOverChecker() {
        return new GameOverChecker();
    }

    @Bean
    public EndingEvaluator endingEvaluator() {
        return new EndingEvaluator();
    }

    @Bean
    public DayEndProcessor dayEndProcessor(
            ConflictTriggers conflictTriggers,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        return new DayEndProcessor(conflictTriggers, gameOverChecker, endingEvaluator, npcLifecycleEngine);
    }

    @Bean
    public GameStateViewMapper gameStateViewMapper(List<GameAction> gameActions) {
        return new GameStateViewMapper(gameActions);
    }
}
