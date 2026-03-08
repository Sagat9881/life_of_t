package ru.lifegame.backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.spec.DataDrivenAction;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpec;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.model.session.GameOverChecker;
import ru.lifegame.backend.infrastructure.game.GameEngineAdapter;

import java.util.List;

@Configuration
public class DomainConfig {

    private static final Logger log = LoggerFactory.getLogger(DomainConfig.class);

    @Bean
    public PlayerActionSpecLoader playerActionSpecLoader() {
        return new PlayerActionSpecLoader();
    }

    @Bean
    public ActionProvider actionProvider(PlayerActionSpecLoader loader) {
        List<PlayerActionSpec> specs = loader.loadAll();
        List<GameAction> actions = specs.stream()
                .map(spec -> (GameAction) new DataDrivenAction(spec))
                .toList();
        log.info("\u2705 Player actions loaded from XML: {} actions", actions.size());
        actions.forEach(a -> log.info("   \u2022 {} — {}", a.type().code(), a.type().label()));
        return new GameEngineAdapter(actions);
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
}
