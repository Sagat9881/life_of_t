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
 * Spring configuration for NPC engine beans.
 * All concrete NPC data comes from XML — this config only wires abstract engine components.
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
    public NpcRegistry npcRegistry(NarrativeContentLoader loader) {
        var specs = loader.loadAllNpcSpecsFromDirectory("narrative/npc-behavior");
        return NpcRegistry.fromSpecs(specs);
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph(NarrativeContentLoader loader) {
        var edges = loader.loadRelationshipEdges("narrative/npc-relationships.xml");
        return NpcRelationshipGraph.fromEdges(edges);
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(NpcRelationshipGraph graph) {
        return new CrossNpcTriggerEngine(graph);
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(
            NpcRegistry registry,
            NpcUtilityBrain brain,
            CrossNpcTriggerEngine crossNpcTriggerEngine) {
        return new NpcLifecycleEngine(registry, brain, crossNpcTriggerEngine);
    }
}
