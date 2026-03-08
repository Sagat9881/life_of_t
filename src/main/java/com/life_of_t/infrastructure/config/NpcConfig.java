package com.life_of_t.infrastructure.config;

import com.life_of_t.domain.npc.engine.ConditionEvaluator;
import com.life_of_t.domain.npc.engine.NpcLifecycleEngine;
import com.life_of_t.domain.npc.engine.NpcRegistry;
import com.life_of_t.domain.npc.engine.NpcUtilityBrain;
import com.life_of_t.domain.npc.graph.CrossNpcTriggerEngine;
import com.life_of_t.domain.npc.graph.NpcRelationshipGraph;
import com.life_of_t.infrastructure.narrative.NarrativeContentLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring configuration for the data-driven NPC engine.
 * Loads all NPC specs from narrative XML at startup.
 * Engine has ZERO knowledge of concrete NPC names or actions.
 */
@Configuration
public class NpcConfig {

    @Bean
    public NarrativeContentLoader narrativeContentLoader() throws IOException {
        NarrativeContentLoader loader = new NarrativeContentLoader();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Load all NPC behavior XMLs
        Resource[] npcResources = resolver.getResources("classpath:narrative/npc-behavior/*.xml");
        for (Resource resource : npcResources) {
            try (InputStream is = resource.getInputStream()) {
                loader.loadNpcSpec(is);
            }
        }

        // Load cross-NPC relations if exists
        Resource[] crossResources = resolver.getResources("classpath:narrative/cross-npc-relations.xml");
        for (Resource resource : crossResources) {
            if (resource.exists()) {
                loader.loadFromClasspath("narrative/cross-npc-relations.xml");
            }
        }

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
    public NpcRegistry npcRegistry(NarrativeContentLoader loader) {
        return NpcRegistry.fromSpecs(loader.getNpcSpecs());
    }

    @Bean
    public NpcRelationshipGraph npcRelationshipGraph(NarrativeContentLoader loader) {
        return new NpcRelationshipGraph(loader.getRelationshipEdges());
    }

    @Bean
    public CrossNpcTriggerEngine crossNpcTriggerEngine(NarrativeContentLoader loader) {
        return new CrossNpcTriggerEngine(loader.getCrossNpcConditions());
    }

    @Bean
    public NpcLifecycleEngine npcLifecycleEngine(
            NpcRegistry registry,
            NpcUtilityBrain brain,
            NpcRelationshipGraph graph,
            CrossNpcTriggerEngine crossNpcTriggerEngine
    ) {
        return new NpcLifecycleEngine(registry, brain);
    }
}
