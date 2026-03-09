package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.spec.DataDrivenActionProvider;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.conflict.spec.ConflictLoader;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.ConflictManager;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.model.session.GameOverChecker;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import java.util.List;

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
    public ConflictLoader conflictLoader() {
        return new ConflictLoader();
    }

    @Bean
    public ConflictEngine conflictEngine(ConflictLoader loader) {
        List<ConflictSpec> specs = loader.loadFromClasspath("game-config/conflicts.xml");
        return new ConflictEngine(specs);
    }

    @Bean
    public ConflictManager conflictManager() {
        return new ConflictManager();
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
            ConflictEngine conflictEngine,
            ConflictManager conflictManager,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        return new DayEndProcessor(conflictEngine, conflictManager, gameOverChecker, endingEvaluator, npcLifecycleEngine);
    }
}
