package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.engine.NarrativeContentLoader;
import ru.lifegame.backend.domain.engine.NarrativeEventEngine;
import ru.lifegame.backend.domain.engine.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.NpcSpecLoader;
import ru.lifegame.backend.domain.npc.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;

@Configuration
public class NpcConfig {

    // ── NPC engine beans ─────────────────────────────────────

    @Bean
    public NpcSpecLoader npcSpecLoader() {
        return new NpcSpecLoader();
    }

    @Bean
    public NpcUtilityBrain npcUtilityBrain() {
        return new NpcUtilityBrain();
    }

    @Bean
    public NpcRegistry npcRegistry() {
        return new NpcRegistry();
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        return new NpcLifecycleEngine(registry, brain);
    }

    // ── Narrative engine beans ────────────────────────────────

    @Bean
    public NarrativeContentLoader narrativeContentLoader() {
        return new NarrativeContentLoader();
    }

    @Bean
    public NarrativeEventEngine narrativeEventEngine(NarrativeContentLoader loader) {
        return new NarrativeEventEngine(loader.eventSpecs());
    }

    @Bean
    public NarrativeQuestEngine narrativeQuestEngine(NarrativeContentLoader loader) {
        return new NarrativeQuestEngine(loader.questSpecs());
    }
}
