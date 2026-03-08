package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.spec.DataDrivenActionProvider;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.model.session.GameOverChecker;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

@Configuration
public class DomainConfig {

    @Bean
    public PlayerActionSpecLoader playerActionSpecLoader() {
        return new PlayerActionSpecLoader();
    }

    @Bean
    public ActionProvider actionProvider(PlayerActionSpecLoader specLoader) {
        return new DataDrivenActionProvider(specLoader);
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
}
