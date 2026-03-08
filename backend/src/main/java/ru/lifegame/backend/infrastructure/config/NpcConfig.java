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
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;
import ru.lifegame.backend.infrastructure.narrative.NarrativeContentLoader;

import java.util.List;

/**
 * Spring configuration for the data-driven NPC engine.
 * All NPC content is loaded from XML specs — no hardcoded names or behaviors.
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
        List<NpcSpec> specs = loader.loadAllNpcSpecs();
        return NpcRegistry.fromSpecs(specs);
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph(NarrativeContentLoader loader) {
        List<NpcRelationshipEdge> edges = loader.loadRelationshipEdges();
        return NpcRelationshipGraph.fromEdges(edges);
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(
            NarrativeContentLoader loader,
            NpcRelationshipGraph graph) {
        List<CrossNpcConditionSpec> triggers = loader.loadCrossNpcTriggers();
        return new CrossNpcTriggerEngine(triggers, graph);
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(
            NpcRegistry registry,
            NpcUtilityBrain brain,
            CrossNpcTriggerEngine crossNpcTriggerEngine) {
        return new NpcLifecycleEngine(registry, brain, crossNpcTriggerEngine);
    }
}
