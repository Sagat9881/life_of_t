package ru.lifegame.backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.lifegame.backend.domain.narrative.NarrativeContentLoader;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.NpcSpecLoader;
import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.util.List;

/**
 * Loads all narrative content (NPC specs, events, quests) from XML
 * when the Spring application context is fully ready.
 *
 * Boot sequence:
 *   1. Load NPC specs → register into NpcRegistry
 *   2. Load EventSpecs + QuestSpecs via NarrativeContentLoader
 *   3. Re-populate NarrativeEventEngine and NarrativeQuestEngine
 *      (both beans are constructed empty in DomainConfig to avoid
 *       circular deps; this step feeds them the real data)
 */
@Component
public class NarrativeBootstrap {

    private static final Logger log = LoggerFactory.getLogger(NarrativeBootstrap.class);

    private final NpcSpecLoader npcSpecLoader;
    private final NpcRegistry npcRegistry;
    private final NarrativeContentLoader narrativeContentLoader;
    private final NarrativeEventEngine narrativeEventEngine;
    private final NarrativeQuestEngine narrativeQuestEngine;

    public NarrativeBootstrap(NpcSpecLoader npcSpecLoader,
                              NpcRegistry npcRegistry,
                              NarrativeContentLoader narrativeContentLoader,
                              NarrativeEventEngine narrativeEventEngine,
                              NarrativeQuestEngine narrativeQuestEngine) {
        this.npcSpecLoader = npcSpecLoader;
        this.npcRegistry = npcRegistry;
        this.narrativeContentLoader = narrativeContentLoader;
        this.narrativeEventEngine = narrativeEventEngine;
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

        // 2. Load narrative events and quests from classpath XMLs
        try {
            narrativeContentLoader.loadFromClasspath();
            int eventCount = narrativeContentLoader.eventSpecs().size();
            int questCount = narrativeContentLoader.questSpecs().size();
            log.info("✅ Narrative content loaded: {} events, {} quests", eventCount, questCount);

            // 3. Feed loaded specs into the engines
            // NarrativeEventEngine and NarrativeQuestEngine are constructed empty
            // (see DomainConfig) — reload them here with real data.
            narrativeEventEngine.reloadSpecs(narrativeContentLoader.eventSpecs());
            narrativeQuestEngine.reloadSpecs(narrativeContentLoader.questSpecs());
            log.info("✅ Engines populated: {} event specs, {} quest specs",
                    eventCount, questCount);
        } catch (Exception e) {
            log.error("❌ Failed to load narrative content: {}", e.getMessage(), e);
        }

        log.info("─── Narrative Bootstrap: complete ───");
    }
}
