package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.spec.DataDrivenActionProvider;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.ending.EndingEngine;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.ConflictManager;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import java.io.InputStream;
import java.util.List;

/**
 * Spring bean declarations for core domain services.
 *
 * NarrativeEventEngine and NarrativeQuestEngine are constructed empty here.
 * NarrativeBootstrap feeds them real specs from GameContentService on
 * ApplicationReadyEvent (after @PostConstruct on GameContentService completes).
 */
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

    /**
     * Spring automatically injects ApplicationContext as ResourceLoader.
     * ConflictLoader receives it via constructor — no ApplicationContextAware needed.
     */
    @Bean
    public ConflictLoader conflictLoader(ResourceLoader resourceLoader) {
        return new ConflictLoader(resourceLoader);
    }

    @Bean
    public ConflictEngine conflictEngine(ConflictLoader loader) {
        List<ConflictSpec> specs = loader.loadAll();
        return new ConflictEngine(specs);
    }

    /**
     * ConflictManager requires ConflictEngine to resolve specs for
     * startConflict() and applyTactic().
     */
    @Bean
    public ConflictManager conflictManager(ConflictEngine conflictEngine) {
        return new ConflictManager(conflictEngine);
    }

    @Bean
    public EndingEngine endingEngine() {
        try {
            InputStream xmlStream = new ClassPathResource("narrative/endings.xml").getInputStream();
            return new EndingEngine(xmlStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load endings.xml", e);
        }
    }

    @Bean
    public DayEndProcessor dayEndProcessor(
            ConflictEngine conflictEngine,
            ConflictManager conflictManager,
            EndingEngine endingEngine,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        return new DayEndProcessor(conflictEngine, conflictManager, endingEngine, npcLifecycleEngine);
    }

    /**
     * Constructed empty — populated by NarrativeBootstrap.onApplicationReady().
     * Evaluated by ExecutePlayerActionService and EndDayService on every tick.
     */
    @Bean
    public NarrativeEventEngine narrativeEventEngine() {
        return new NarrativeEventEngine(List.of());
    }

    /**
     * Constructed empty — populated by NarrativeBootstrap.onApplicationReady().
     * Evaluated by ExecutePlayerActionService on every player action.
     */
    @Bean
    public NarrativeQuestEngine narrativeQuestEngine() {
        return new NarrativeQuestEngine(List.of());
    }
}
