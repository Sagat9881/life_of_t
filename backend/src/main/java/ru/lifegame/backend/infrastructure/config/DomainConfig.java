package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import ru.lifegame.backend.application.service.GameContentService;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.spec.DataDrivenActionProvider;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpecLoader;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.ending.EndingEngine;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.ConflictManager;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.narrative.NarrativeContentLoader;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import java.io.InputStream;
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
        List<ConflictSpec> specs = loader.loadConflicts("game-config/conflicts.xml");
        return new ConflictEngine(specs);
    }

    @Bean
    public ConflictManager conflictManager() {
        return new ConflictManager();
    }

    @Bean
    public EndingEngine endingEngine() {
        try {
            InputStream xmlStream = new ClassPathResource("game-data/endings.xml").getInputStream();
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
     * NarrativeContentLoader is the raw XML reader — constructed empty,
     * populated by NarrativeBootstrap on ApplicationReadyEvent.
     */
    @Bean
    public NarrativeContentLoader narrativeContentLoader() {
        return new NarrativeContentLoader();
    }

    /**
     * NarrativeEventEngine is constructed empty here.
     * NarrativeBootstrap feeds it the loaded EventSpecs after parsing.
     * This two-phase init (construct → populate) avoids a circular dependency
     * with GameContentService and keeps domain objects free of Spring.
     */
    @Bean
    public NarrativeEventEngine narrativeEventEngine() {
        return new NarrativeEventEngine(List.of());
    }

    /**
     * NarrativeQuestEngine is constructed empty here.
     * NarrativeBootstrap feeds it the loaded QuestSpecs after parsing.
     */
    @Bean
    public NarrativeQuestEngine narrativeQuestEngine() {
        return new NarrativeQuestEngine(List.of());
    }
}
