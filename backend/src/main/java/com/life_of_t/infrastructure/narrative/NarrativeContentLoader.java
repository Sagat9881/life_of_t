package com.life_of_t.infrastructure.narrative;

import com.life_of_t.domain.npc.NpcRegistry;
import com.life_of_t.domain.npc.NpcRelationshipGraph;
import com.life_of_t.domain.npc.CrossNpcTriggerEngine;
import com.life_of_t.domain.npc.spec.NpcSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Central loader for all narrative content from XML specifications.
 * Scans narrative/ directory at startup and builds domain objects.
 * This is the single entry point for all XML-driven content.
 */
public class NarrativeContentLoader {

    private static final Logger log = LoggerFactory.getLogger(NarrativeContentLoader.class);

    private final NpcSpecLoader npcSpecLoader;
    private List<NpcSpec> npcSpecs;
    private NpcRelationshipGraph relationshipGraph;
    private CrossNpcTriggerEngine crossNpcTriggerEngine;

    public NarrativeContentLoader(NpcSpecLoader npcSpecLoader) {
        this.npcSpecLoader = npcSpecLoader;
    }

    /**
     * Load all narrative content. Called once at application startup.
     */
    public void loadAll() {
        log.info("Loading narrative content from XML specifications...");

        // Load NPC specs
        this.npcSpecs = npcSpecLoader.loadAll();
        log.info("Loaded {} NPC specifications", npcSpecs.size());

        long named = npcSpecs.stream().filter(s -> "named".equals(s.type())).count();
        long filler = npcSpecs.stream().filter(s -> "filler".equals(s.type())).count();
        log.info("  Named NPCs: {}, Filler NPCs: {}", named, filler);

        // Build relationship graph from specs
        this.relationshipGraph = buildRelationshipGraph();
        log.info("Built NPC relationship graph with {} edges", relationshipGraph.allEdges().size());

        // Cross-NPC triggers (will be loaded from separate XML in future)
        this.crossNpcTriggerEngine = CrossNpcTriggerEngine.empty();
        log.info("Cross-NPC trigger engine initialized (triggers will load from XML)");

        log.info("Narrative content loading complete.");
    }

    /**
     * Create a fresh NpcRegistry for a new game session.
     * Each session gets its own mutable NPC instances.
     */
    public NpcRegistry createSessionRegistry() {
        return NpcRegistry.fromSpecs(npcSpecs);
    }

    /**
     * Create a fresh relationship graph for a new game session.
     */
    public NpcRelationshipGraph createSessionRelationshipGraph() {
        return buildRelationshipGraph();
    }

    public CrossNpcTriggerEngine crossNpcTriggerEngine() {
        return crossNpcTriggerEngine;
    }

    public List<NpcSpec> npcSpecs() {
        return npcSpecs;
    }

    private NpcRelationshipGraph buildRelationshipGraph() {
        NpcRelationshipGraph graph = new NpcRelationshipGraph();

        // Build edges between all named NPCs of category "human"
        List<NpcSpec> namedHumans = npcSpecs.stream()
                .filter(s -> "named".equals(s.type()) && "human".equals(s.category()))
                .toList();

        for (int i = 0; i < namedHumans.size(); i++) {
            for (int j = i + 1; j < namedHumans.size(); j++) {
                graph.addEdge(
                        namedHumans.get(i).id(),
                        namedHumans.get(j).id(),
                        Map.of("respect", 50, "tension", 20, "familiarity", 60)
                );
            }
        }

        // Animals don't have inter-NPC relationships by default
        // (can be extended via XML cross-npc-relationships spec)

        return graph;
    }
}
