package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;
import ru.lifegame.backend.domain.npc.engine.NpcLifecycleEngine;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;
import ru.lifegame.backend.domain.npc.engine.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.graph.CrossNpcTriggerEngine;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.infrastructure.narrative.NarrativeContentLoader;

import java.util.List;

/**
 * Spring configuration for the data-driven NPC engine.
 * All NPC content comes from XML — this config only wires the engine beans.
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
        List<NpcSpec> specs = loader.loadAllNpcSpecsFromDirectory("narrative/npc-behavior");
        NpcRegistry registry = new NpcRegistry();
        registry.initializeFromSpecs(specs);
        return registry;
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph(NarrativeContentLoader loader) {
        List<NpcRelationshipEdge> edges = loader.loadRelationshipEdges("narrative/npc-relationships.xml");
        NpcRelationshipGraph graph = new NpcRelationshipGraph();
        for (NpcRelationshipEdge edge : edges) {
            graph.addEdge(edge);
        }
        return graph;
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
