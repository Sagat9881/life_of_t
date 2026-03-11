package ru.lifegame.backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.lifegame.backend.application.service.GameContentService;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.NpcSpecLoader;
import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.util.List;

/**
 * Bootstraps all narrative content when the Spring context is fully ready.
 *
 * Boot sequence:
 *   1. Load NPC specs from XML → register into NpcRegistry
 *   2. Feed already-parsed QuestSpecs from GameContentService into NarrativeQuestEngine
 *      (GameContentService parses XMLs once via @PostConstruct — no second scan)
 *
 * NarrativeEventEngine no longer needs bootstrapping — specs are passed
 * per-call from GameContentService, keeping a single source of truth.
 */
@Component
public class NarrativeBootstrap {

    private static final Logger log = LoggerFactory.getLogger(NarrativeBootstrap.class);

    private final NpcSpecLoader npcSpecLoader;
    private final NpcRegistry npcRegistry;
    private final GameContentService gameContentService;
    private final NarrativeQuestEngine narrativeQuestEngine;

    public NarrativeBootstrap(NpcSpecLoader npcSpecLoader,
                              NpcRegistry npcRegistry,
                              GameContentService gameContentService,
                              NarrativeQuestEngine narrativeQuestEngine) {
        this.npcSpecLoader = npcSpecLoader;
        this.npcRegistry = npcRegistry;
        this.gameContentService = gameContentService;
        this.narrativeQuestEngine = narrativeQuestEngine;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("─── Narrative Bootstrap: loading game content ───");

        // 1. Load and register NPC specs
        try {
            List<NpcSpec> npcSpecs = npcSpecLoader.loadAll();
            npcRegistry.registerFromSpecs(npcSpecs);
            log.info("✅ NPC specs loaded: {} NPCs registered (named: {}, filler: {})",
                    npcRegistry.size(),
                    npcSpecs.stream().filter(NpcSpec::isNamed).count(),
                    npcSpecs.stream().filter(NpcSpec::isFiller).count());
            npcSpecs.forEach(spec ->
                    log.info("   • {} [{}] — {} actions, {} schedule slots",
                            spec.displayName(), spec.type(),
                            spec.actions().size(), spec.schedule().size()));
        } catch (Exception e) {
            log.error("❌ Failed to load NPC specs: {}", e.getMessage(), e);
        }

        // 2. Feed quest engine from GameContentService (already parsed at @PostConstruct)
        try {
            var quests = gameContentService.getAllQuests();
            narrativeQuestEngine.reloadSpecs(quests);
            log.info("✅ Quest engine populated: {} quest specs", quests.size());
        } catch (Exception e) {
            log.error("❌ Failed to populate quest engine: {}", e.getMessage(), e);
        }

        log.info("─── Narrative Bootstrap: complete ───");
    }
}
