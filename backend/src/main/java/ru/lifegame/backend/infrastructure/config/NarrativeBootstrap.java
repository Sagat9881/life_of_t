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
 * This is the single bootstrap point that wires XML data → domain objects.
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
        log.info("\u2500\u2500\u2500 Narrative Bootstrap: loading game content \u2500\u2500\u2500");

        // 1. Load NPC specs from classpath:narrative/npc-behavior/*.xml
        try {
            List<NpcSpec> npcSpecs = npcSpecLoader.loadAll();
            npcRegistry.registerFromSpecs(npcSpecs);
            log.info("\u2705 NPC specs loaded: {} NPCs registered (named: {}, filler: {})",
                    npcRegistry.size(),
                    npcSpecs.stream().filter(NpcSpec::isNamed).count(),
                    npcSpecs.stream().filter(NpcSpec::isFiller).count());
            npcSpecs.forEach(spec ->
                    log.info("   \u2022 {} [{}] \u2014 {} actions, {} schedule slots",
                            spec.displayName(), spec.type(),
                            spec.actions().size(), spec.schedule().size()));
        } catch (Exception e) {
            log.error("\u274C Failed to load NPC specs: {}", e.getMessage(), e);
        }

        // 2. Load narrative events and quests from classpath
        try {
            narrativeContentLoader.loadFromClasspath();
            log.info("\u2705 Narrative content loaded: {} events, {} quests",
                    narrativeContentLoader.eventSpecs().size(),
                    narrativeContentLoader.questSpecs().size());
        } catch (Exception e) {
            log.error("\u274C Failed to load narrative content: {}", e.getMessage(), e);
        }

        log.info("\u2500\u2500\u2500 Narrative Bootstrap: complete \u2500\u2500\u2500");
    }
}
