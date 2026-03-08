package com.sagat9881.lifeoft.domain.npc;

import java.util.*;

/**
 * Bidirectional relationship graph between NPC pairs.
 * Each edge has three axes: respect, tension, familiarity.
 * The graph is data-driven — edges are defined in XML specs
 * and updated by game events.
 */
public class NpcRelationshipGraph {

    private final Map<String, NpcRelationshipEdge> edges;

    public NpcRelationshipGraph() {
        this.edges = new HashMap<>();
    }

    /**
     * Set or update a relationship edge between two NPCs.
     * Edge key is always sorted alphabetically to ensure bidirectionality.
     */
    public void setEdge(String npcA, String npcB, int respect, int tension, int familiarity) {
        String key = edgeKey(npcA, npcB);
        edges.put(key, new NpcRelationshipEdge(npcA, npcB, respect, tension, familiarity));
    }

    public Optional<NpcRelationshipEdge> getEdge(String npcA, String npcB) {
        return Optional.ofNullable(edges.get(edgeKey(npcA, npcB)));
    }

    /**
     * Modify tension between two NPCs by delta.
     */
    public void addTension(String npcA, String npcB, int delta) {
        String key = edgeKey(npcA, npcB);
        NpcRelationshipEdge existing = edges.get(key);
        if (existing != null) {
            edges.put(key, existing.withTension(
                    Math.max(0, Math.min(100, existing.tension() + delta))));
        }
    }

    /**
     * Modify respect between two NPCs by delta.
     */
    public void addRespect(String npcA, String npcB, int delta) {
        String key = edgeKey(npcA, npcB);
        NpcRelationshipEdge existing = edges.get(key);
        if (existing != null) {
            edges.put(key, existing.withRespect(
                    Math.max(0, Math.min(100, existing.respect() + delta))));
        }
    }

    /**
     * Check if any NPC pair has tension above threshold — used by CrossNpcTriggerEngine.
     */
    public List<NpcRelationshipEdge> highTensionEdges(int threshold) {
        return edges.values().stream()
                .filter(e -> e.tension() > threshold)
                .toList();
    }

    public Collection<NpcRelationshipEdge> allEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    private String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a;
    }

    /**
     * A single relationship edge between two NPCs.
     */
    public record NpcRelationshipEdge(
            String npcA,
            String npcB,
            int respect,
            int tension,
            int familiarity
    ) {
        public NpcRelationshipEdge withTension(int newTension) {
            return new NpcRelationshipEdge(npcA, npcB, respect, newTension, familiarity);
        }

        public NpcRelationshipEdge withRespect(int newRespect) {
            return new NpcRelationshipEdge(npcA, npcB, newRespect, tension, familiarity);
        }

        public NpcRelationshipEdge withFamiliarity(int newFamiliarity) {
            return new NpcRelationshipEdge(npcA, npcB, respect, tension, newFamiliarity);
        }
    }
}
