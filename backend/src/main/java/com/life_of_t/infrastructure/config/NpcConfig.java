package com.life_of_t.infrastructure.config;

import com.life_of_t.domain.npc.ConditionEvaluator;
import com.life_of_t.domain.npc.NpcLifecycleEngine;
import com.life_of_t.domain.npc.NpcUtilityBrain;
import com.life_of_t.infrastructure.narrative.NarrativeContentLoader;
import com.life_of_t.infrastructure.narrative.NpcSpecLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Spring configuration for NPC engine.
 * Wires up XML loaders, domain engines, and lifecycle components.
 */
@Configuration
public class NpcConfig {

    @Bean
    public NpcSpecLoader npcSpecLoader() {
        return new NpcSpecLoader();
    }

    @Bean
    public NarrativeContentLoader narrativeContentLoader(NpcSpecLoader npcSpecLoader) {
        NarrativeContentLoader loader = new NarrativeContentLoader(npcSpecLoader);
        loader.loadAll();
        return loader;
    }

    @Bean
    public ConditionEvaluator conditionEvaluator() {
        return new ConditionEvaluator();
    }

    @Bean
    public NpcUtilityBrain npcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        return new NpcUtilityBrain(conditionEvaluator);
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(NpcUtilityBrain utilityBrain) {
        return new NpcLifecycleEngine(utilityBrain);
    }
}
