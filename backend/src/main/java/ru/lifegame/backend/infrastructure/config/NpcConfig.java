package ru.lifegame.backend.infrastructure.config;

import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.NpcUtilityBrain;

@Configuration
public class NpcConfig {

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
