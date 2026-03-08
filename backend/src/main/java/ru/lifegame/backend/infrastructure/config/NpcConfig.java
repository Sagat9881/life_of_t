package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;
import ru.lifegame.backend.domain.npc.engine.NpcLifecycleEngine;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;
import ru.lifegame.backend.domain.npc.engine.NpcUtilityBrain;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;
import ru.lifegame.backend.domain.npc.graph.CrossNpcTriggerEngine;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.infrastructure.narrative.NarrativeContentLoader;

import java.util.List;

@Configuration
public class NpcConfig {

    @Bean
    public NarrativeContentLoader narrativeContentLoader() {
        return new NarrativeContentLoader();
    }

    @Bean
    public List<NpcSpec> npcSpecs(NarrativeContentLoader loader) {
        return loader.loadAllNpcSpecs();
    }

    @Bean
    public NpcRegistry npcRegistry(List<NpcSpec> npcSpecs) {
        return NpcRegistry.fromSpecs(npcSpecs);
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
    public NpcLifecycleEngine npcLifecycleEngine(NpcUtilityBrain brain) {
        return new NpcLifecycleEngine(brain);
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph() {
        return new NpcRelationshipGraph();
    }

    @Bean
    public List<CrossNpcConditionSpec> crossNpcConditions(NarrativeContentLoader loader) {
        return loader.loadCrossNpcConditions();
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(
            NpcRelationshipGraph graph,
            List<CrossNpcConditionSpec> conditions) {
        return new CrossNpcTriggerEngine(graph, conditions);
    }
}
