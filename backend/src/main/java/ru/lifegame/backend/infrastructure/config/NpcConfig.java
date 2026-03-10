package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.NpcSpecLoader;
import ru.lifegame.backend.domain.npc.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;

/**
 * Spring bean declarations for the NPC subsystem.
 *
 * Narrative engine beans (NarrativeEventEngine, NarrativeQuestEngine) live in
 * DomainConfig and are populated by NarrativeBootstrap on ApplicationReadyEvent.
 */
@Configuration
public class NpcConfig {

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
}
