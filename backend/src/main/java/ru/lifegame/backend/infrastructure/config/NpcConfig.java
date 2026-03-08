package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.engine.*;
import ru.lifegame.backend.domain.npc.graph.*;
import ru.lifegame.backend.infrastructure.narrative.NarrativeContentLoader;

/**
 * Spring configuration for NPC engine beans.
 * All NPC content is loaded from XML — no hardcoded NPC names here.
 */
@Configuration
public class NpcConfig {

    @Bean
    public NarrativeContentLoader narrativeContentLoader() {
        NarrativeContentLoader loader = new NarrativeContentLoader();
        loader.loadFromClasspath("narrative");
        return loader;
    }

    @Bean
    public NpcRegistry npcRegistry(NarrativeContentLoader loader) {
        return NpcRegistry.fromSpecs(loader.getNpcSpecs());
    }

    @Bean
    public ConditionEvaluator conditionEvaluator() {
        return new ConditionEvaluator();
    }

    @Bean
    public NpcUtilityBrain npcUtilityBrain(ConditionEvaluator evaluator) {
        return new NpcUtilityBrain(evaluator);
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph(NarrativeContentLoader loader) {
        return NpcRelationshipGraph.fromEdges(loader.getRelationshipEdges());
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(NpcRelationshipGraph graph) {
        return new CrossNpcTriggerEngine(graph);
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(
            NpcRegistry registry,
            NpcUtilityBrain brain,
            CrossNpcTriggerEngine crossNpcTrigger) {
        return new NpcLifecycleEngine(registry, brain, crossNpcTrigger);
    }
}
