package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;
import ru.lifegame.backend.domain.npc.engine.NpcLifecycleEngine;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;
import ru.lifegame.backend.domain.npc.engine.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.graph.CrossNpcTriggerEngine;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.infrastructure.narrative.NarrativeContentLoader;

/**
 * Spring configuration for the data-driven NPC engine.
 * Wires all NPC components together — no hardcoded NPC names anywhere.
 */
@Configuration
public class NpcConfig {

    @Bean
    public NarrativeContentLoader narrativeContentLoader() {
        return new NarrativeContentLoader();
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
    public NpcRegistry npcRegistry() {
        return new NpcRegistry();
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph() {
        return new NpcRelationshipGraph();
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(NpcRelationshipGraph graph) {
        return new CrossNpcTriggerEngine(graph);
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(
            NpcRegistry registry,
            NpcUtilityBrain brain,
            NpcRelationshipGraph graph,
            CrossNpcTriggerEngine crossNpcTriggerEngine) {
        return new NpcLifecycleEngine(registry, brain, graph, crossNpcTriggerEngine);
    }
}
